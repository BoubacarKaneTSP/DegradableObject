package eu.cloudbutton.dobj.Benchmark.Tester;

import eu.cloudbutton.dobj.Counter.AbstractCounter;
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

    public FactoryTester(Object object, int[] ratios, CountDownLatch latch) {
        this.object = object;
        this.ratios = ratios;
        this.latch = latch;
    }

    public Tester createTester() throws ClassNotFoundException{

        if (object instanceof AbstractMap)
            return new MapTester((AbstractMap) object, ratios, latch);
        else if (object instanceof AbstractSet)
            return new SetTester((AbstractSet) object, ratios, latch);
        else if (object instanceof AbstractQueue)
            return new QueueTester((AbstractQueue) object, ratios, latch);
        else if (object instanceof AbstractList)
            return new ListTester((AbstractList) object, ratios, latch);
        else if (object instanceof AbstractCounter)
            return new CounterTester((AbstractCounter) object, ratios, latch);
        else if (object instanceof Noop)
            return new NoopTester((Noop) object, ratios, latch);
        else
            throw new ClassNotFoundException("The Tester for"+ object.getClass() +" may not exists");
    }

}
