package eu.cloudbutton.dobj.Benchmark.Tester;

import java.util.Deque;
import java.util.NoSuchElementException;
import java.util.concurrent.CountDownLatch;

public class DequeTester extends Tester<Deque> {

    public DequeTester(Deque list, int[] ratios, CountDownLatch latch, long nbOps) {
        super(list, ratios, latch, nbOps);
    }

    @Override
    protected void test(opType type) {
        int n = random.nextInt(ITEM_PER_THREAD);
        long iid = Thread.currentThread().getId() * 1000000000L + n;

        switch (type) {
            case ADD:
                object.addFirst(iid);
                break;
            case REMOVE:
                try{
                    object.removeLast();
                } catch (NoSuchElementException e) {
                }
                break;
            case READ:
                object.toArray();
                break;
        }
    }
}
