package io.garage.manifest.grpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.garage.common.Validators;
import io.garage.exceptions.DomainException;
import io.garage.manifest.model.Manifest;
import io.garage.manifest.model.Metadata;
import io.garage.manifest.service.SpecDispatcher;
import io.grpc.stub.StreamObserver;
import io.quarkus.grpc.GrpcService;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GrpcService
public class ManifestGrpcService extends ManifestServiceGrpc.ManifestServiceImplBase {

    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    private static final Logger log = LoggerFactory.getLogger(ManifestGrpcService.class);

    @Inject
    SpecDispatcher specDispatcher;

    @Override
    public void applyManifest(ManifestRequest request, StreamObserver<ApplyManifestResponse> response) {
        Uni.createFrom().item(() ->
                        QuarkusTransaction.requiringNew().call(() -> {
                            Validators.validateYamlContent(request.getYamlContent());

                            Manifest manifest = yamlMapper.readValue(
                                    request.getYamlContent().toByteArray(),
                                    Manifest.class);

                            for (Metadata<?> metadata : manifest.metadata) {
                                specDispatcher.dispatch(metadata.spec);
                            }

                            return ApplyManifestResponse.newBuilder().build();
                        })
                ).runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
                .onFailure(e -> !(e instanceof DomainException)).invoke(e ->
                        log.error("Unexpected error to create user: {}", e.getMessage(), e))
                .subscribe().with(resp -> {
                            response.onNext(resp);
                            response.onCompleted();
                        }, response::onError
                );
    }
}
