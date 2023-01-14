package eu.cloudbutton.dobj.benchmark.tester;

import eu.cloudbutton.dobj.Noop;
import eu.cloudbutton.dobj.incrementonly.Counter;
import eu.cloudbutton.dobj.register.AtomicWriteOnceReference;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class FactoryTester {

    private final Object object;
    private final int[] ratios;
    private final CountDownLatch latch;
    private boolean useCollisionKey;
    private final int max_item_per_thread;

    public FactoryTester(Object object, int[] ratios, CountDownLatch latch, boolean useCollisionKey, int max_item_per_thread) {
        this.object = object;
        this.ratios = ratios;
        this.latch = latch;
        this.useCollisionKey = useCollisionKey;
        this.max_item_per_thread = max_item_per_thread;
    }


    public Tester createTester() throws ClassNotFoundException{

        if (object instanceof Map)
            return new MapTester((Map) object, ratios, latch, useCollisionKey, max_item_per_thread);
        else if (object instanceof Set)
            return new SetTester((Set) object, ratios, latch, useCollisionKey, max_item_per_thread);
        else if (object instanceof Queue)
            return new QueueTester((Queue) object, ratios, latch);
        else if (object instanceof AbstractList)
            return new ListTester((AbstractList) object, ratios, latch);
        else if (object instanceof Counter)
            return new CounterTester((Counter) object, ratios, latch);
        else if (object instanceof Noop)
            return new NoopTester((Noop) object, ratios, latch);
        else if (object instanceof AtomicWriteOnceReference)
            return new AtomicWriteOnceReferenceTester((AtomicWriteOnceReference) object, ratios, latch);
        else if (object instanceof AtomicReference)
            return new AtomicReferenceTester((AtomicReference) object, ratios, latch);
        else
            throw new ClassNotFoundException("The Tester for"+ object.getClass() +" may not exists");
    }

}
