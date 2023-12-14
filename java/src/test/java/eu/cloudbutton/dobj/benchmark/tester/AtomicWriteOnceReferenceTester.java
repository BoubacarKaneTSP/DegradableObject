package eu.cloudbutton.dobj.benchmark.tester;

import eu.cloudbutton.dobj.benchmark.Microbenchmark;
import eu.cloudbutton.dobj.incrementonly.BoxedLong;
import eu.cloudbutton.dobj.register.AtomicWriteOnceReference;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CountDownLatch;

public class AtomicWriteOnceReferenceTester extends Tester<AtomicWriteOnceReference>{
    public AtomicWriteOnceReferenceTester(AtomicWriteOnceReference object, int[] ratios, CountDownLatch latch) {
        super(object, ratios, latch);
    }

    @Override
    protected long test(Microbenchmark.opType type) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        long startTime = 0L, endTime = 0L;

        for (int i = 0; i < 10000; i++) {
            Microbenchmark.map.put(Thread.currentThread().getName() + i, i);
        }
        switch (type) {
            case ADD:
                object.set(1);
                break;
            case REMOVE:
                startTime = System.nanoTime();
                for (int i = 0; i < nbRepeat; i++) {
                }
                endTime = System.nanoTime();
                break;
            case READ:
                startTime = System.nanoTime();
                for (int i = 0; i < nbRepeat; i++) {
                    object.get();
                }
                endTime = System.nanoTime();

                break;
        }
        for (int i = 0; i < 10000; i++) {
            Microbenchmark.map.remove(Thread.currentThread().getName() + i);
        }

        return (endTime - startTime);
    }

}
