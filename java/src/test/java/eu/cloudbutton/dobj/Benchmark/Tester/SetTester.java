package eu.cloudbutton.dobj.Benchmark.Tester;

import java.util.AbstractSet;
import java.util.concurrent.CountDownLatch;

public class SetTester extends Tester<AbstractSet> {

    public SetTester(AbstractSet object, int[] ratios, CountDownLatch latch, long nbOps) {
        super(object, ratios, latch, nbOps);
    }

    @Override
    protected void test(char type) {

        int n = random.nextInt(ITEM_PER_THREAD);
        long iid = Thread.currentThread().getId() * 1000000000L + n;
        switch (type) {
            case 'a':
                object.add(iid);
                break;
            case 'r':
                object.remove(iid);
                break;
            case 'c':
                object.contains(iid);
                break;
        }
    }
}
