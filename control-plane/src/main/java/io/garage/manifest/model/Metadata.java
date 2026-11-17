package io.garage.manifest.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

public class Metadata<T extends Spec> {

    public String type;
    public T spec;

    public Metadata(String type, T specNode) {
        this.type = type;
        this.spec = specNode;
    }

    @JsonCreator
    public static Metadata<Spec> fromYaml(
            @JsonProperty("type") String type,
            @JsonProperty("spec") JsonNode specNode) {
        return new Metadata<>(type, Spec.parse(type, specNode));
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public T getSpec() { return this.spec; }
    public void setSpec(T spec) { this.spec = spec; }
}
