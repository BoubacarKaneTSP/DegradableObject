package eu.cloudbutton.dobj.Benchmark.Tester;

import java.util.*;
import java.util.concurrent.CountDownLatch;

public class ListTester extends Tester<AbstractList> {

    public ListTester(AbstractList list, int[] ratios, CountDownLatch latch) {
        super(list, ratios, latch);
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
                Collection<Long> ret = new ArrayList<>();

                Iterator<Long> it = object.iterator();
                int i = 0;

                while (it.hasNext() && i < 50) {
                    ret.add(it.next());
                    i++;
                }
                break;
        }
    }
}