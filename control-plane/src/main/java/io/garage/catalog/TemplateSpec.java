package io.garage.catalog;

import java.util.Map;

public record TemplateSpec(
        String name,
        String description,
        String gitRepositoryUrl,
        String stack,
        Map<String, Object> defaultParameters
) {}
