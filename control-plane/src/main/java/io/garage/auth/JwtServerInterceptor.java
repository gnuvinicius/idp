package io.garage.auth;

import io.grpc.*;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;

@ApplicationScoped
public class JwtServerInterceptor implements ServerInterceptor {

    private static final Metadata.Key<String> AUTH_HEADER =
            Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);

    @Inject
    JWTParser jwtParser;

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        String fullMethodName = call.getMethodDescriptor().getFullMethodName();

        if (fullMethodName.equals("garage.auth.UserService/Login")) {
            return next.startCall(call, headers);
        }

        String authHeader = headers.get(AUTH_HEADER);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            call.close(Status.UNAUTHENTICATED.withDescription("Token de autenticação ausente ou inválido"), headers);
            return new ServerCall.Listener<ReqT>() {
            };
        }

        String token = authHeader.substring(7);
        try {
            JsonWebToken jwt = jwtParser.parse(token);
            // Contexto validado; pode ser propagado via io.grpc.Context se necessário
            return next.startCall(call, headers);
        } catch (ParseException e) {
            call.close(Status.UNAUTHENTICATED.withDescription("Assinatura de token inválida ou expirada"), headers);
            return new ServerCall.Listener<ReqT>() {
            };
        }
    }
}
