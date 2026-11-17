package io.garage.catalog.grpc;

import io.quarkus.grpc.GrpcService;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;

@GrpcService
@Authenticated
public class TemplateGrpcService implements TemplateService {

    @Override
    public Uni<ListTemplatesResponse> listTemplate(ListTemplatesRequest request) {
        return null;
    }

    @Override
    public Uni<DeleteTemplateResponse> deleteTemplate(DeleteTemplateRequest request) {
        return null;
    }
}
