package eu.cloudbutton.dobj.Benchmark.Tester;

import java.util.AbstractSet;
import java.util.concurrent.CountDownLatch;

public class SetTester extends Tester<AbstractSet> {

    public SetTester(AbstractSet set, int[] ratios, CountDownLatch latch) {
        super(set, ratios, latch);
    }

    @Override
    protected long test(opType type) {

        long startTime = 0L, endTime = 0L;

        int rand = random.nextInt(ITEM_PER_THREAD);
//        String iid = Thread.currentThread().getId()  + Long.toString(rand);
        long iid = Thread.currentThread().getId() * 1_000_000_000L + rand;

        switch (type) {
            case ADD:
                startTime = System.nanoTime();
                object.add(iid);
                endTime = System.nanoTime();
                break;
            case REMOVE:
                startTime = System.nanoTime();
                object.remove(iid);
                endTime = System.nanoTime();
                break;
            case READ:
                startTime = System.nanoTime();
                object.contains(iid);
                endTime = System.nanoTime();
                break;
        }

        return endTime - startTime;
    }
}
