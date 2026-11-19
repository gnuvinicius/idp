package io.garage.manifest.service;

import io.garage.catalog.entity.Template;
import io.garage.manifest.dispatcher.ApplyContext;
import io.garage.manifest.dispatcher.ApplyResult;
import io.garage.manifest.dispatcher.ManifestyTypeEnum;
import io.garage.manifest.dispatcher.SpecApplier;
import io.garage.manifest.model.TemplateSpec;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TemplateSpecApplier implements SpecApplier<TemplateSpec> {

    @Override
    public Class<TemplateSpec> specType() {
        return TemplateSpec.class;
    }

    @Override
    public ApplyResult apply(TemplateSpec spec, ApplyContext context) {
        Template existing = Template.findByName(spec.name());

        if (existing == null) {
            Template entity = new Template(spec);
            entity.persistAndFlush();
            return ApplyResult.created(ManifestyTypeEnum.TEMPLATE, spec.name());
        } else {
            return ApplyResult.updated(ManifestyTypeEnum.TEMPLATE, spec.name());
        }
    }

}
