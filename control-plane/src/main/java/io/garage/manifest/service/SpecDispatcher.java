package io.garage.manifest.service;

import io.garage.auth.User;
import io.garage.auth.UserRoleEnum;
import io.garage.manifest.model.Spec;
import io.grpc.Status;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@ApplicationScoped
public class SpecDispatcher {

    private final Map<Class<? extends Spec>, SpecApplier<?>> appliers;

    @Inject
    JsonWebToken jwt;

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

        ApplyContext context = buildContext();
        return applier.applyRaw(spec, context);
    }

    private ApplyContext buildContext() {
        if (jwt == null || jwt.getSubject() == null) {
            throw Status.UNAUTHENTICATED
                    .withDescription("Invalid JWT token")
                    .asRuntimeException();
        }

        Long userId = Long.valueOf(jwt.getSubject());
        User entity = User.findById(userId);
        if (entity == null) {
            throw Status.UNAUTHENTICATED.asRuntimeException();
        }

        UserRoleEnum role = jwt.getGroups().contains("ADMIN")
                ? UserRoleEnum.ADMIN
                : UserRoleEnum.DEVELOPER;

        return new ApplyContext(entity.id, entity.username, entity.email, role, entity);
    }
}
