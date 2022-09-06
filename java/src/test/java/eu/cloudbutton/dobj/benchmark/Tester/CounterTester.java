package eu.cloudbutton.dobj.benchmark.Tester;

import eu.cloudbutton.dobj.incrementonly.AbstractCounter;

import java.util.concurrent.CountDownLatch;

public class CounterTester extends Tester<AbstractCounter> {

    public CounterTester(AbstractCounter counter, int[] ratios, CountDownLatch latch) {
        super(counter, ratios, latch);
    }

    @Override
    protected long test(opType type) {

        long startTime = 0L, endTime = 0L;

        switch (type) {
            case ADD:
            case REMOVE:
                startTime = System.nanoTime();
                for (int i = 0; i < nbRepeat; i++) {
                    object.incrementAndGet();
                }
                endTime = System.nanoTime();
                break;
            case READ:
                startTime = System.nanoTime();
                for (int i = 0; i < nbRepeat; i++) {
                    object.read();
                }
                endTime = System.nanoTime();

                break;
        }

        return (endTime - startTime)/nbRepeat;
    }
}
