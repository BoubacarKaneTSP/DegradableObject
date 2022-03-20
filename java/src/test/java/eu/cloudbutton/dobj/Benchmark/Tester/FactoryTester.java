package eu.cloudbutton.dobj.Benchmark.Tester;

import eu.cloudbutton.dobj.types.AbstractCounter;

import java.util.AbstractMap;
import java.util.AbstractQueue;
import java.util.AbstractSet;
import java.util.Deque;
import java.util.concurrent.CountDownLatch;

public class FactoryTester {

    private final Object object;
    private final int[] ratios;
    private final CountDownLatch latch;
    private final long nbOps;

    public FactoryTester(Object object, int[] ratios, CountDownLatch latch, long nbOps) {
        this.object = object;
        this.ratios = ratios;
        this.latch = latch;
        this.nbOps = nbOps;
    }

    public NoopTester createNoopTester() {
        return new NoopTester((eu.cloudbutton.dobj.types.Noop) object, ratios, latch, nbOps);
    }

    public CounterTester createAbstractCounterTester() {
        return new CounterTester((AbstractCounter) object, ratios, latch, nbOps);
    }

    public SetTester createAbstractSetTester() {
        return new SetTester((AbstractSet) object, ratios, latch, nbOps);
    }


    public QueueTester createAbstractQueueTester() {
        return new QueueTester((AbstractQueue) object, ratios, latch, nbOps);
    }

    public MapTester createAbstractMapTester() {
        return new MapTester((AbstractMap) object, ratios, latch, nbOps);
    }

    public DequeTester createAbstractCollectionTester() {
        return new DequeTester((Deque) object, ratios, latch, nbOps);
    }

}
