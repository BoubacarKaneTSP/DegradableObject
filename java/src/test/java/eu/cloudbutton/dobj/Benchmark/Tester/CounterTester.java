package eu.cloudbutton.dobj.Benchmark.Tester;

import eu.cloudbutton.dobj.types.AbstractCounter;

import java.util.concurrent.CountDownLatch;

public class CounterTester extends Tester<AbstractCounter> {

    public CounterTester(AbstractCounter counter, int[] ratios, CountDownLatch latch, long nbOps) {
        super(counter, ratios, latch, nbOps);
    }

    @Override
    protected void test(opType type) {

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
