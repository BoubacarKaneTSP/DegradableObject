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
    protected void test(opType type, long iid) {

        switch (type) {
            case ADD:
                object.offer(iid);
                break;
            case REMOVE:
                object.poll();
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
