package eu.cloudbutton.dobj.Benchmark.Tester;

import eu.cloudbutton.dobj.Benchmark.Benchmark;

import java.util.AbstractMap;

public class FactoryFiller {

    private final Benchmark benchmark;
    private final Object object;
    private final long nbOps;

    public FactoryFiller(Benchmark benchmark, Object object, long nbOps) {
        this.benchmark = benchmark;
        this.object = object;
        this.nbOps = nbOps;
    }

    public MapFiller createAbstractMapFiller() {
        return new MapFiller((AbstractMap) object, nbOps);
    }
}
