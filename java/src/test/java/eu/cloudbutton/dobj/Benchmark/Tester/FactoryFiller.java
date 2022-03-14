package eu.cloudbutton.dobj.Benchmark.Tester;

import eu.cloudbutton.dobj.Benchmark.Benchmark;

import java.util.AbstractMap;
import java.util.AbstractSet;

public class FactoryFiller {

    private final Object object;
    private final long nbOps;

    public FactoryFiller(Object object, long nbOps) {
        this.object = object;
        this.nbOps = nbOps;
    }

    public MapFiller createAbstractMapFiller() {
        return new MapFiller((AbstractMap) object, nbOps);
    }
    public SetFiller createAbstractSetFiller() { return new SetFiller((AbstractSet) object, nbOps); }
}
