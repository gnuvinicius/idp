package io.garage.manifest.service;

import io.garage.catalog.entity.Project;
import io.garage.catalog.entity.Template;
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
            Project entity = new Project(
                    spec.name(),
                    spec.description(),
                    spec.repositoryUrl(),
                    template,
                    context.userEntity()
            );
            entity.persist();
            return ApplyResult.created(ManifestyTypeEnum.PROJECT, entity.name);
        } else {
            return ApplyResult.unchanged(ManifestyTypeEnum.PROJECT, spec.name());
        }
    }
}
