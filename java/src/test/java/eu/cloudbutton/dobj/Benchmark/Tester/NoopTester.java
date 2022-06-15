package eu.cloudbutton.dobj.Benchmark.Tester;

import eu.cloudbutton.dobj.Noop;

import java.util.concurrent.CountDownLatch;

public class NoopTester extends Tester<Noop> {

    public NoopTester(Noop nope, int[] ratios, CountDownLatch latch) {
        super(nope, ratios, latch);
    }

    @Override
    protected long test(opType type) {
        // no-op

        long startTime = 1L, endTime = 1L;

        int n = random.nextInt(ITEM_PER_THREAD);
        switch (type) {
            case ADD:
                startTime = System.nanoTime();
                for (int i = 0; i < 5000; i++) {
                    n++;
                }
                endTime = System.nanoTime();
                break;
            case REMOVE:
                startTime = System.nanoTime();
                for (int i = 0; i < 5000; i++) {
                    n--;
                }
                endTime = System.nanoTime();
                break;
            case READ:
                startTime = System.nanoTime();
                for (int i = 0; i < 5000; i++) {
                    n += 2;
                }
                endTime = System.nanoTime();

                break;
        }

        return (endTime - startTime)/5000;
    }
}
