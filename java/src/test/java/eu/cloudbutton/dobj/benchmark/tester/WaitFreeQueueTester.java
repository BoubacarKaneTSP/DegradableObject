package eu.cloudbutton.dobj.benchmark.tester;

import eu.cloudbutton.dobj.benchmark.Microbenchmark.opType;
import eu.cloudbutton.dobj.incrementonly.BoxedLong;
import eu.cloudbutton.dobj.queue.WaitFreeQueue;

import java.lang.reflect.InvocationTargetException;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;

public class WaitFreeQueueTester extends Tester<WaitFreeQueue> {

    public WaitFreeQueueTester(WaitFreeQueue list, int[] ratios, CountDownLatch latch) {
        super(list, ratios, latch);
    }

    @Override
    protected long test(opType type) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        long startTime = 0L, endTime = 0L;

        int rand = random.nextInt(Integer.MAX_VALUE);

        WaitFreeQueue.Handle<Integer> h = object.register();
        switch (type) {
            case ADD:
                startTime = System.nanoTime();
                for (int i = 0; i < nbRepeat; i++) {
                    object.enqueue(rand, h);
                    object.dequeue(h);
                }
                endTime = System.nanoTime();
                break;
            case REMOVE:
                startTime = System.nanoTime();
                for (int i = 0; i < nbRepeat; i++) {
                    object.dequeue(h);
                }
                endTime = System.nanoTime();
                break;
            case READ:
                startTime = System.nanoTime();
                for (int i = 0; i < nbRepeat; i++) {
                    object.contains(rand);
                }
                endTime = System.nanoTime();
                break;
        }

        return (endTime - startTime);
    }

}
