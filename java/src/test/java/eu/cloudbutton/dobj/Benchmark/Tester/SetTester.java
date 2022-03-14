package eu.cloudbutton.dobj.Benchmark.Tester;

import java.util.AbstractSet;
import java.util.concurrent.CountDownLatch;

public class SetTester extends Tester<AbstractSet> {

    public SetTester(AbstractSet object, int[] ratios, CountDownLatch latch, long nbOps) {
        super(object, ratios, latch, nbOps);
    }

    @Override
    protected void test(opType type) {

        int n = random.nextInt(ITEM_PER_THREAD);
        long iid = (Thread.currentThread().getId() * 1000000000 + n);
        switch (type) {
            case ADD:
                object.add(iid);
                break;
            case REMOVE:
                object.remove(iid);
                break;
            case READ:
                object.contains(iid);
                break;
        }
    }
}
