package eu.cloudbutton.dobj.benchmark.tester;

import eu.cloudbutton.dobj.counter.Counter;
import eu.cloudbutton.dobj.Noop;

import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.AbstractQueue;
import java.util.AbstractSet;
import java.util.concurrent.CountDownLatch;

public class FactoryTester {

    private final Object object;
    private final int[] ratios;
    private final CountDownLatch latch;
    private boolean useCollisionKey;

    public FactoryTester(Object object, int[] ratios, CountDownLatch latch, boolean useCollisionKey) {
        this.object = object;
        this.ratios = ratios;
        this.latch = latch;
        this.useCollisionKey = useCollisionKey;
    }


    public Tester createTester() throws ClassNotFoundException{

        if (object instanceof AbstractMap)
            return new MapTester((AbstractMap) object, ratios, latch, useCollisionKey);
        else if (object instanceof AbstractSet)
            return new SetTester((AbstractSet) object, ratios, latch, useCollisionKey);
        else if (object instanceof AbstractQueue)
            return new QueueTester((AbstractQueue) object, ratios, latch);
        else if (object instanceof AbstractList)
            return new ListTester((AbstractList) object, ratios, latch);
        else if (object instanceof Counter)
            return new CounterTester((Counter) object, ratios, latch);
        else if (object instanceof Noop)
            return new NoopTester((Noop) object, ratios, latch);
        else
            throw new ClassNotFoundException("The Tester for"+ object.getClass() +" may not exists");
    }

}
