package io.garage.manifest.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.grpc.Status;

public interface Spec {

    ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory());

    default void validateRules() {}

    static Spec parse(String type, JsonNode spec) {
        if (type == null || type.isBlank()) {
            throw Status.INVALID_ARGUMENT
                    .withDescription("Missing required field 'type' in metadata entry")
                    .asRuntimeException();
        }

        if (spec == null || spec.isNull()) {
            throw Status.INVALID_ARGUMENT
                    .withDescription("Missing required field 'spec' in metadata entry")
                    .asRuntimeException();
        }

        Class<? extends Spec> targetClass = resolveTargetClass(type);

        try {
            Spec parsed = YAML_MAPPER.treeToValue(spec, targetClass);
            parsed.validateRules();
            return parsed;
        } catch (JsonProcessingException e) {
            throw Status.INVALID_ARGUMENT
                    .withDescription("Error in YAML syntax: " + e.getMessage())
                    .asRuntimeException();
        }
    }

    private static Class<? extends Spec> resolveTargetClass(String type) {
        return switch (type) {
            case "project" -> ProjectSpec.class;
            case "template" -> TemplateSpec.class;
            default -> throw Status.INVALID_ARGUMENT
                    .withDescription("Unknow metadata type: " + type)
                    .asRuntimeException();
        };
    }
}
