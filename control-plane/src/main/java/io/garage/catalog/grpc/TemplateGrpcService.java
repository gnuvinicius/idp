package io.garage.catalog.grpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.garage.catalog.TemplateManifest;
import io.garage.catalog.TemplateSpec;
import io.garage.catalog.entity.Template;
import io.grpc.Status;
import io.quarkus.grpc.GrpcService;
import io.quarkus.security.Authenticated;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;

import java.io.IOException;
import java.util.Objects;

@GrpcService
@Authenticated
public class TemplateGrpcService implements TemplateService {

    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    @Blocking
    @Transactional
    @RolesAllowed("ADMIN")
    @Override
    public Uni<ApplyTemplatesResponse> applyTemplates(ApplyTemplatesRequest request) {
        if (request.getYamlContent().isEmpty()) {
            throw Status.INVALID_ARGUMENT
                    .withDescription("YAML is empty")
                    .asRuntimeException();
        }

        TemplateManifest manifest = parseManifest(request.getYamlContent().toByteArray());

        if (manifest == null || manifest.templates() == null || manifest.templates().isEmpty()) {
            throw Status.INVALID_ARGUMENT
                    .withDescription("any template find on main key 'templates'")
                    .asRuntimeException();
        }

        int created = 0;
        int updated = 0;
        int unchanged = 0;

        for (TemplateSpec spec : manifest.templates()) {
            Template existing = Template.findByName(spec.name());

            if (existing == null) {
                Template entity = new Template(spec);
                entity.persist();
                created++;
            } else if (hasChanges(existing, spec)) {
                existing.description = spec.description();
                existing.gitRepositoryUrl = spec.gitRepositoryUrl();
                updated++;
            } else {
                unchanged++;
            }
        }

        return Uni.createFrom().item(
                ApplyTemplatesResponse.newBuilder()
                        .setCreated(created)
                        .setUpdated(updated)
                        .setUnchanged(unchanged)
                        .build()
        );
    }

    @Override
    public Uni<ListTemplatesResponse> listTemplate(ListTemplatesRequest request) {
        return null;
    }

    @Override
    public Uni<DeleteTemplateResponse> deleteTemplate(DeleteTemplateRequest request) {
        return null;
    }

    private TemplateManifest parseManifest(byte[] content) {
        try {
            return yamlMapper.readValue(content, TemplateManifest.class);
        } catch (IOException e) {
            throw Status.INVALID_ARGUMENT
                    .withDescription("Error to YAML syntax: " + e.getMessage())
                    .asRuntimeException();
        }
    }

    private boolean hasChanges(Template existing, TemplateSpec spec) {
        return !Objects.equals(existing.description, spec.description())
                || !Objects.equals(existing.gitRepositoryUrl, spec.gitRepositoryUrl());
    }
}
