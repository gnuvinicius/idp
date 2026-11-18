package io.garage.manifest.model;

import java.util.Map;

public record TemplateSpec(
        String name,
        String description,
        String repositoryUrl,
        Map<String, Object> defaultParameters
) implements Spec {}
