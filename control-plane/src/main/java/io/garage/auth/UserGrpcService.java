package io.garage.auth;

import io.garage.auth.grpc.CreateUserRequest;
import io.garage.auth.grpc.CreateUserResponse;
import io.garage.auth.grpc.UserService;
import io.garage.domain.User;
import io.garage.domain.enums.UserRoleEnum;
import io.garage.exceptions.DomainException;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.grpc.GrpcService;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GrpcService
public class UserGrpcService implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserGrpcService.class);

    @Inject
    TokenService tokenService;

    @Override
    @Transactional
    public Uni<CreateUserResponse> createUser(CreateUserRequest request) {
        try {
            return Uni.createFrom().item(() -> {
                    User.findByUsername(request.getUsername())
                            .ifPresent(user -> {
                        throw new DomainException(String.format("user %s already exists", user.username));
                    });

            String passwordEncrypted = BcryptUtil.bcryptHash(request.getPassword());
            User user = new User(request.getUsername(), request.getEmail(), passwordEncrypted, UserRoleEnum.DEVELOPER);
            user.persist();

            return CreateUserResponse.newBuilder()
                    .setId(user.id)
                    .setUsername(user.username)
                    .setEmail(user.email)
                    .setRole(user.role.name())
                    .setCreatedAt(user.createdAt.toString())
                    .build();
            }).runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
        } catch (DomainException e) {
            log.error("error to create user: {}", e.getMessage());
            throw new DomainException(e.getMessage());
        } catch (Exception e) {
            log.error("error to create user: {}", e.getMessage(), e);
            throw e;
        }
    }
}
