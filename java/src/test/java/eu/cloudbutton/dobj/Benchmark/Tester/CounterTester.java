package eu.cloudbutton.dobj.Benchmark.Tester;

import eu.cloudbutton.dobj.types.AbstractCounter;

import java.util.concurrent.CountDownLatch;

public class CounterTester extends Tester<AbstractCounter> {

    public CounterTester(AbstractCounter counter, int[] ratios, CountDownLatch latch) {
        super(counter, ratios, latch);
    }

    @Override
    protected void test(opType type, long iid) {

        switch (type) {
            case ADD:
            case REMOVE:
                object.increment();
                break;
            case READ:
                object.read();
                break;
        }
    }
}
