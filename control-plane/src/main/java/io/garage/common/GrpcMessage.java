package io.garage.common;

import io.garage.exceptions.DomainException;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrpcMessage {

    private static final Logger log = LoggerFactory.getLogger(GrpcMessage.class);

    public static Throwable toGrpcError(Throwable t) {
        if (t instanceof StatusRuntimeException sre) {
            return sre;
        }
        if (t instanceof DomainException de) {
            log.warn("{}: {}", "domain exception:", de.getMessage());
            return Status.ALREADY_EXISTS
                    .withDescription(de.getMessage())
                    .asRuntimeException();
        }

        log.error("{}: {}", "domain exception:", t.getMessage(), t);
        return Status.INTERNAL
                .withDescription(t.getClass().getSimpleName() + ": " + t.getMessage())
                .asRuntimeException();
    }
}
