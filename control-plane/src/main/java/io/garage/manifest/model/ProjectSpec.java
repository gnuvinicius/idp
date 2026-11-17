package io.garage.manifest.model;

public record ProjectSpec(
        String name,
        String description,
        String repositoryUrl,
        String template
) implements Spec {

}
