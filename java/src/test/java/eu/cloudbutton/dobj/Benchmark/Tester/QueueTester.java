package eu.cloudbutton.dobj.Benchmark.Tester;

import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;

public class QueueTester extends Tester<AbstractQueue> {

    public QueueTester(AbstractQueue list, int[] ratios, CountDownLatch latch) {
        super(list, ratios, latch);
    }

    @Override
    protected long test(opType type) {

        long startTime = 0L, endTime = 0L, iid = 0L;

        if (type == opType.ADD || type == opType.READ){
            int rand = random.nextInt(ITEM_PER_THREAD);
            iid = Thread.currentThread().getId() * 1_000_000_000L + rand;
        }

        switch (type) {
            case ADD:
                startTime = System.nanoTime();
                object.offer(iid);
                endTime = System.nanoTime();
                break;
            case REMOVE:
                startTime = System.nanoTime();
                object.poll();
                endTime = System.nanoTime();
                break;
            case READ:
                startTime = System.nanoTime();
                    object.contains(iid);
                endTime = System.nanoTime();
                Collection<Long> ret = new ArrayList<>();

                Iterator<Long> it = object.iterator();
                int i = 0;

                while (it.hasNext() && i < 50) {
                    ret.add(it.next());
                    i++;
                }
                break;
        }

        return endTime - startTime;
    }
}
