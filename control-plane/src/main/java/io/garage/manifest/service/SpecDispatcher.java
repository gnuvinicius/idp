package io.garage.manifest.service;

import io.garage.manifest.model.Spec;
import io.grpc.Status;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@ApplicationScoped
public class SpecDispatcher {

    private final Map<Class<? extends Spec>, SpecApplier<?>> appliers;

    @Inject
    public SpecDispatcher(Instance<SpecApplier<?>> applierInstances) {
        this.appliers = applierInstances.stream()
                .collect(Collectors.toMap(SpecApplier::specType, Function.identity()));
    }

    public ApplyResult dispatch(Spec spec) {
        SpecApplier<?> applier = appliers.get(spec.getClass());
        if (applier == null) {
            throw Status.UNIMPLEMENTED
                    .withDescription("No applier registered for spec: " + spec.getClass().getSimpleName())
                    .asRuntimeException();
        }
        return applier.applyRaw(spec);
    }
}
