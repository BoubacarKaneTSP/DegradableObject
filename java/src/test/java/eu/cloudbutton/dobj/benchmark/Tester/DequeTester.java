package eu.cloudbutton.dobj.benchmark.Tester;

import java.lang.reflect.InvocationTargetException;
import java.util.Deque;
import java.util.NoSuchElementException;
import java.util.concurrent.CountDownLatch;
import eu.cloudbutton.dobj.benchmark.Microbenchmark.opType;
import eu.cloudbutton.dobj.incrementonly.BoxedLong;


public class DequeTester extends Tester<Deque> {

    public DequeTester(Deque list, int[] ratios, CountDownLatch latch) {
        super(list, ratios, latch);
    }

    long startTime = 0L, endTime = 0L;

    int rand = random.nextInt(ITEM_PER_THREAD);
    long iid = Thread.currentThread().getId() * 1_000_000_000L + rand;

    @Override
    protected long test(opType type) {

        switch (type) {
            case ADD:
                startTime = System.nanoTime();
                object.addFirst(iid);
                endTime = System.nanoTime();
                break;
            case REMOVE:
                try{
                    startTime = System.nanoTime();
                    object.removeLast();
                    endTime = System.nanoTime();
                } catch (NoSuchElementException e) {
                }
                break;
            case READ:
                startTime = System.nanoTime();
                object.toArray();
                endTime = System.nanoTime();
                break;
        }

        return endTime - startTime;
    }

    @Override
    protected long test(opType type, BoxedLong boxedLong) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        return 0;
    }
}
