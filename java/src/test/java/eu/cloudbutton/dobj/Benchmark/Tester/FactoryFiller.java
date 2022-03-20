package eu.cloudbutton.dobj.Benchmark.Tester;

import eu.cloudbutton.dobj.types.AbstractCounter;

import java.util.AbstractMap;
import java.util.AbstractQueue;
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
    public QueueFiller createAbstractQueueFiller() { return new QueueFiller((AbstractQueue) object,nbOps); }
    public CounterFiller createAbstractCounterFiller() { return new CounterFiller((AbstractCounter) object, nbOps); }
}
