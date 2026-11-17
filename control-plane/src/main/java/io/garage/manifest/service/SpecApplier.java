package io.garage.manifest.service;

import io.garage.manifest.model.Spec;

public interface SpecApplier<T extends Spec> {

    Class<T> specType();

    ApplyResult apply(T spec);

    default ApplyResult applyRaw(Spec spec) {
        return apply(specType().cast(spec));
    }
}
