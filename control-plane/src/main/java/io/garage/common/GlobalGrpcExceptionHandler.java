package io.garage.common;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.quarkus.grpc.ExceptionHandler;
import io.quarkus.grpc.ExceptionHandlerProvider;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class GlobalGrpcExceptionHandler implements ExceptionHandlerProvider {

    private static final Logger log = LoggerFactory.getLogger(GlobalGrpcExceptionHandler.class);

    @Override
    public <ReqT, RespT> ExceptionHandler<ReqT, RespT> createHandler(
            ServerCall.Listener<ReqT> listener,
            ServerCall<ReqT, RespT> serverCall,
            Metadata metadata) {

        return new ExceptionHandler<>(listener, serverCall, metadata) {
            @Override
            protected void handleException(Throwable t, ServerCall<ReqT, RespT> call, Metadata metadata) {
                Throwable mapped = transform(t);
                Metadata trailers = Status.trailersFromThrowable(mapped);
                call.close(Status.fromThrowable(mapped), trailers != null ? trailers : new Metadata());
            }
        };
    }

    @Override
    public Throwable transform(Throwable t) {
        if (t instanceof StatusRuntimeException) {
            return t;
        }

        log.error("Falha não tratada na chamada gRPC", t);

        Throwable root = getRootCause(t);
        String detail = root.getClass().getSimpleName() + ": " + root.getMessage();
        return Status.INTERNAL.withDescription(detail).withCause(t).asRuntimeException();
    }

    private Throwable getRootCause(Throwable t) {
        Throwable current = t;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current;
    }
}

