package io.garage.manifest.service;

import io.garage.manifest.model.Spec;

public interface SpecApplier<T extends Spec> {

    Class<T> specType();

    void apply(T spec);

    default void applyRaw(Spec spec) {
        apply(specType().cast(spec));
    }
}
