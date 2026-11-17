package io.garage.manifest.service;

import io.garage.catalog.entity.Template;
import io.garage.manifest.model.TemplateSpec;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TemplateSpecApplier implements SpecApplier<TemplateSpec> {

    @Override
    public Class<TemplateSpec> specType() {
        return TemplateSpec.class;
    }

    @Override
    public ApplyResult apply(TemplateSpec spec) {
        Template existing = Template.findByName(spec.name());

        if (existing == null) {
            Template entity = new Template(spec);
            entity.persist();
            return ApplyResult.created(ManifestyTypeEnum.TEMPLATE, spec.name());
        } else {
            return ApplyResult.updated(ManifestyTypeEnum.TEMPLATE, spec.name());
        }
    }

}
