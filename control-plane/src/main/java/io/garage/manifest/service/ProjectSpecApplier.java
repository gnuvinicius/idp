package io.garage.manifest.service;

import io.garage.catalog.entity.Project;
import io.garage.catalog.entity.Template;
import io.garage.manifest.dispatcher.ApplyContext;
import io.garage.manifest.dispatcher.ApplyResult;
import io.garage.manifest.dispatcher.ManifestyTypeEnum;
import io.garage.manifest.dispatcher.SpecApplier;
import io.garage.manifest.model.ProjectSpec;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProjectSpecApplier implements SpecApplier<ProjectSpec> {

    @Override
    public Class<ProjectSpec> specType() {
        return ProjectSpec.class;
    }

    @Override
    public ApplyResult apply(ProjectSpec spec, ApplyContext context) {
        Project existing = Project.findByName(spec.name());
        Template template = Template.findByName(spec.template());

        if (existing == null) {
            Project entity = spec.toEntity(template, context.userEntity());
            return ApplyResult.created(ManifestyTypeEnum.PROJECT, entity.name);
        } else if (existChanges(existing, spec)) {
            spec.updateEntity(existing, template, context.userEntity());
            return ApplyResult.updated(ManifestyTypeEnum.PROJECT, spec.name());
        } else {
            return ApplyResult.unchanged(ManifestyTypeEnum.PROJECT, spec.name());
        }
    }

    private boolean existChanges(Project entity, ProjectSpec spec) {
        return !entity.name.equals(spec.name())
                || !entity.description.equals(spec.description())
                || !entity.templateUsed.name.equals(spec.template())
                || !entity.repositoryUrl.equals(spec.repositoryUrl());
    }
}
