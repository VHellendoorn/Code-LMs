package sassa.util;

import com.sun.jdi.InterfaceType;
import kaptainwutax.featureutils.structure.*;
import kaptainwutax.mcutils.state.Dimension;
import kaptainwutax.mcutils.version.MCVersion;
import sun.security.krb5.internal.crypto.Des;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StructureProvider {

    StructureSupplier structureSupplier;
    MCVersion version;
    Dimension dimension;
    int minValue;

    public StructureProvider(StructureSupplier structureSupplier, MCVersion version, Dimension dimension, int minValue){
        this.structureSupplier = structureSupplier;
        this.version = version;
        this.dimension = dimension;
        this.minValue = minValue;
    }

    public StructureSupplier getStructureSupplier() {
        return this.structureSupplier;
    }

    public MCVersion getVersion() {
        return this.version;
    }

    public Dimension getDimension() {
        return this.dimension;
    }

    public int getMinimumValue(){
        return this.minValue;
    }

    public void setMinimumValue(int minValue) {
        this.minValue = minValue;
    }

    public interface StructureSupplier {
        RegionStructure<?, ?> create(MCVersion version);
    }

}
