package io.garage.auth;

import io.garage.auth.grpc.*;
import io.garage.common.GrpcMessage;
import io.garage.exceptions.DomainException;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.grpc.GrpcService;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.smallrye.jwt.build.Jwt;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;


/**
 * Annotations:
 * <br>
 * On CreateUser: Method to create new user, isn't exists annotation transactions because the transaction is managed
 * from QuarkusTransaction.requiringNew.
 * Since gRPC methods using `StreamObserver` in Quarkus run by default on the event loop, including
 * transactional in the method signature causes Quarkus to attempt to open a JDBC transaction on the
 * event loop, which typically results in a `BlockingOperationNotAllowedException`.
 */
@GrpcService
public class AuthGrpcService extends AuthServiceGrpc.AuthServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(AuthGrpcService.class);

    @Override
    public void createUser(CreateUserRequest request, StreamObserver<CreateUserResponse> response) {
        Uni.createFrom().item(() ->
                        QuarkusTransaction.requiringNew().call(() -> {
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
                        })
                ).runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
                .onFailure(DomainException.class).invoke(e ->
                        log.error("error to create user: {}", e.getMessage()))
                .onFailure(e -> !(e instanceof DomainException)).invoke(e ->
                        log.error("Unexpected error to create user: {}", e.getMessage(), e))
                .subscribe().with(
                        resp -> {
                            response.onNext(resp);
                            response.onCompleted();
                        },
                        error -> response.onError(GrpcMessage.toGrpcError(error)));
    }

    @Override
    public void login(LoginRequest request, StreamObserver<LoginResponse> response) {
        log.info("login");
        Uni.createFrom().item(() ->
                        QuarkusTransaction.requiringNew().call(() -> {
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
                        })
                ).runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
                .onFailure(StatusRuntimeException.class).invoke(e ->
                        log.error("failed login attempt for {}: {}", request.getUsername(), e.getMessage()))
                .onFailure(e -> !(e instanceof StatusRuntimeException)).invoke(e ->
                        log.error("unexpected error during login: {}", e.getMessage(), e))
                .subscribe().with(
                        item -> {
                            response.onNext(item);
                            response.onCompleted();
                        },
                        response::onError
                );
    }

    /**
     * se precisar ler atributos lazy do usuario, deve colocar o esse bloco de codigo dentro do
     * bloco transacional para evitar LazyInitialzationException
     */
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
