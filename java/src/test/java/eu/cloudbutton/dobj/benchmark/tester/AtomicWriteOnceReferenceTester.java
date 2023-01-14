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

        return (endTime - startTime);
    }

    @Override
    protected long test(Microbenchmark.opType type, BoxedLong boxedLong) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        return 0;
    }
}
