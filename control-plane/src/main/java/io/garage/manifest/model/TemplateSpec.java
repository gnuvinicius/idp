package io.garage.manifest.model;

import java.util.Map;

public record TemplateSpec(
        String name,
        String description,
        String gitRepositoryUrl,
        Map<String, Object> defaultParameters
) implements Spec {}
