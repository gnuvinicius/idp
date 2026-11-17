package io.garage.manifest.model;

import java.util.List;

public class Manifest {

    public String version;
    public List<Metadata<? extends Spec>> metadata;
}
