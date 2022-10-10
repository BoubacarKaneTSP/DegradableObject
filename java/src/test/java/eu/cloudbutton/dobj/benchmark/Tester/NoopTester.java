package eu.cloudbutton.dobj.benchmark.Tester;

import eu.cloudbutton.dobj.Noop;
import eu.cloudbutton.dobj.benchmark.Microbenchmark.opType;
import eu.cloudbutton.dobj.incrementonly.BoxedLong;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CountDownLatch;

public class NoopTester extends Tester<Noop> {

    public NoopTester(Noop nope, int[] ratios, CountDownLatch latch) {
        super(nope, ratios, latch);
    }

    @Override
    protected long test(opType type) {
        // no-op

        long startTime = 1L, endTime = 1L;
        int val = 200_000;

        int n = random.nextInt(ITEM_PER_THREAD);
        switch (type) {
            case ADD:
                startTime = System.nanoTime();
                for (int i = 0; i < val; i++) {
                    n = n + 1;
                }
                endTime = System.nanoTime();
                break;
            case REMOVE:
                startTime = System.nanoTime();
                for (int i = 0; i < val; i++) {
                    n = n - 1;
                }
                endTime = System.nanoTime();
                break;
            case READ:
                startTime = System.nanoTime();
                for (int i = 0; i < val; i++) {
                    n += 2;
                }
                endTime = System.nanoTime();

                break;
        }

        return (endTime - startTime)/val;
    }

    @Override
    protected long test(opType type, BoxedLong boxedLong) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        return 0;
    }
}
