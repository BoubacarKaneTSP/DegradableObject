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

    public FactoryTester(Object object, int[] ratios, CountDownLatch latch) {
        this.object = object;
        this.ratios = ratios;
        this.latch = latch;
    }

    public NoopTester createNoopTester() {
        return new NoopTester((eu.cloudbutton.dobj.types.Noop) object, ratios, latch);
    }

    public CounterTester createAbstractCounterTester() {
        return new CounterTester((AbstractCounter) object, ratios, latch);
    }

    public SetTester createAbstractSetTester() {
        return new SetTester((AbstractSet) object, ratios, latch);
    }


    public QueueTester createAbstractQueueTester() {
        return new QueueTester((AbstractQueue) object, ratios, latch);
    }

    public MapTester createAbstractMapTester() {
        return new MapTester((AbstractMap) object, ratios, latch);
    }

    public DequeTester createAbstractCollectionTester() {
        return new DequeTester((Deque) object, ratios, latch);
    }

}
