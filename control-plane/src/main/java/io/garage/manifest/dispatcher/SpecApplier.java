package io.garage.manifest.dispatcher;

import io.garage.manifest.model.Spec;

public interface SpecApplier<T extends Spec> {

    Class<T> specType();

    ApplyResult apply(T spec, ApplyContext context);

    default ApplyResult applyRaw(Spec spec, ApplyContext context) {
        return apply(specType().cast(spec), context);
    }
}
