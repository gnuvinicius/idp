package io.garage.auth;

import io.garage.auth.grpc.*;
import io.garage.exceptions.DomainException;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.grpc.GrpcService;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.jwt.build.Jwt;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.unchecked.Unchecked;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;

@GrpcService
public class UserGrpcService extends UserServiceGrpc.UserServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(UserGrpcService.class);

    @Override
    @Transactional
    public void createUser(CreateUserRequest request, StreamObserver<CreateUserResponse> response) {
        try {
            Uni.createFrom().item(() -> {
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
                    }).runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
                    .subscribe().with(resp -> {
                        response.onNext(resp);
                        response.onCompleted();
                    }, response::onError);
        } catch (DomainException e) {
            log.error("error to create user: {}", e.getMessage());
            throw new DomainException(e.getMessage());
        } catch (Exception e) {
            log.error("error to create user: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Blocking
    @Override
    public void login(LoginRequest request, StreamObserver<LoginResponse> response) {
        log.info("login");
        Uni.createFrom().item(Unchecked.supplier(() -> {
                    try {
                        User user = User.<User>find("username", request.getUsername())
                                .firstResult();

                        if (user == null || !BcryptUtil.matches(request.getPassword(), user.passwordHash)) {
                            throw Status.UNAUTHENTICATED
                                    .withDescription("invalid credentials")
                                    .asRuntimeException();
                        }

                        Instant expiry = Instant.now().plus(Duration.ofHours(8));
                        String token = generateToken(user);

                        return LoginResponse.newBuilder()
                                .setToken(token)
                                .setExpiresAt(expiry.getEpochSecond())
                                .build();
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        throw e;
                    }
                }))
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
                .subscribe().with(item -> {
                            response.onNext(item);
                            response.onCompleted();
                        }, response::onError

                );
    }

    private String generateToken(User user) {
        return Jwt
                .subject(user.id.toString())
                .claim("username", user.username)
                .claim("email", user.email)
                .upn(user.email)
                .groups(user.role.toString())
                .expiresIn(Duration.ofHours(8))
                .sign();
    }
}
