package eu.cloudbutton.dobj.benchmark.Tester;

import eu.cloudbutton.dobj.incrementonly.BoxedLong;
import eu.cloudbutton.dobj.incrementonly.Counter;
import eu.cloudbutton.dobj.benchmark.Microbenchmark.opType;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CountDownLatch;

public class CounterTester extends Tester<Counter> {

    public CounterTester(Counter counter, int[] ratios, CountDownLatch latch) {
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

        return (endTime - startTime);
    }

    @Override
    protected long test(opType type, ThreadLocal<BoxedLong> boxedLong) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        return 0;
    }
}
