package eu.cloudbutton.dobj.Benchmark.Tester;

import java.util.AbstractMap;
import java.util.concurrent.CountDownLatch;

public class MapTester extends Tester<AbstractMap> {

    public MapTester(AbstractMap object, int[] ratios, CountDownLatch latch, long nbOps) {
        super(object, ratios, latch, nbOps);
        System.out.println(object.keySet().size());
    }

    @Override
    protected void test(char type) {

        int n = random.nextInt(ITEM_PER_THREAD);
        long iid = Thread.currentThread().getId() * 1000000000L + n;
        switch (type) {
            case 'a':
                object.put(iid, iid);
                break;
            case 'r':
                object.remove(iid);
                break;
            case 'c':
                object.get(iid);
                break;
        }
    }
}
