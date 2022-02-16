package eu.cloudbutton.dobj.Benchmark.Tester;

import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;

public class ListTester extends Tester<AbstractQueue> {

    public ListTester(AbstractQueue list, int[] ratios, CountDownLatch latch, long nbOps) {
        super(list, ratios, latch, nbOps);
    }

    @Override
    protected void test(char type) {
        int n = random.nextInt(ITEM_PER_THREAD);
        long iid = Thread.currentThread().getId() * 1000000000L + n;

        switch (type) {
            case 'a':
                object.offer(iid);
                break;
            case 'r':
//                    object.poll();
                break;
            case 'c':
//                    object.contains(iid);
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
