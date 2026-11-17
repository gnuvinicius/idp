package io.garage.common;

import com.google.protobuf.ByteString;
import io.grpc.Status;

public abstract class Validators {

    public static void validateYamlContent(ByteString yamlContent) {
        if (yamlContent.isEmpty()) {
            throw Status.INVALID_ARGUMENT
                    .withDescription("YAML is empty")
                    .asRuntimeException();
        }
    }
}
