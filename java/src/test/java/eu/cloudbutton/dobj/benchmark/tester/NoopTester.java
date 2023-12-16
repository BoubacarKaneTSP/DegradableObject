package eu.cloudbutton.dobj.benchmark.tester;

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

        int n = random.nextInt(ITEM_PER_THREAD);
        switch (type) {
            case ADD:
                startTime = System.nanoTime();
                for (int i = 0; i < nbRepeat; i++) {
                    if (n%42 == 0) n = n + 1;
                    else n *= n;
                    n += n%42;
                }
                endTime = System.nanoTime();
                break;
            case REMOVE:
                startTime = System.nanoTime();
                for (int i = 0; i < nbRepeat; i++) {
                    if (n%42 == 0) n = n - 1;
                    else n *= n;
                    n += n%42;
                }
                endTime = System.nanoTime();
                break;
            case READ:
                startTime = System.nanoTime();
                for (int i = 0; i < nbRepeat; i++) {
                    if (n%42 == 0) n += 2;
                    else n *= n;
                    n += n%42;
                }
                endTime = System.nanoTime();

                break;
        }

        return (endTime - startTime)/nbRepeat;
    }

}
