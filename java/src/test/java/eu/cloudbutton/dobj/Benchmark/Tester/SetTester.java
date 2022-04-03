package eu.cloudbutton.dobj.Benchmark.Tester;

import java.util.AbstractSet;
import java.util.concurrent.CountDownLatch;

public class SetTester extends Tester<AbstractSet> {

    public SetTester(AbstractSet set, int[] ratios, CountDownLatch latch) {
        super(set, ratios, latch);
    }

    @Override
    protected void test(opType type, long iid) {

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
