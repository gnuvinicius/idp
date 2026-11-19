package io.garage.manifest.model;

import io.garage.auth.User;
import io.garage.catalog.entity.Project;
import io.garage.catalog.entity.Template;

public record ProjectSpec(
        String name,
        String description,
        String repositoryUrl,
        String template
) implements Spec {

    public Project toEntity(Template template, User user) {
        Project entity = new Project(
                this.name,
                this.description,
                this.repositoryUrl,
                template,
                user
        );
        entity.persistAndFlush();
        return entity;
    }

    public void updateEntity(Project entity, Template template, User user) {
        entity.name = this.name;
        entity.description = this.description;
        entity.repositoryUrl = this.repositoryUrl;
        entity.templateUsed = template;
        entity.user = user;
        entity.persistAndFlush();
    }
}
