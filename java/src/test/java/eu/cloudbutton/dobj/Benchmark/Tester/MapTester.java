package eu.cloudbutton.dobj.Benchmark.Tester;

import eu.cloudbutton.dobj.types.CollisionKey;

import java.util.AbstractMap;
import java.util.concurrent.CountDownLatch;

public class MapTester extends Tester<AbstractMap> {

    public MapTester(AbstractMap object, int[] ratios, CountDownLatch latch, long nbOps) {
        super(object, ratios, latch, nbOps);
    }

    @Override
    protected void test(opType type) {

        int n = random.nextInt(ITEM_PER_THREAD);
        long iid = Thread.currentThread().getId() * 1000000000L + n;
        CollisionKey collisionKey = new CollisionKey(Long.toString(iid));

        switch (type) {
            case ADD:
                object.put(collisionKey, Long.toString(iid));
//                object.put(Long.toString(iid), Long.toString(iid));
                break;
            case REMOVE:
                object.remove(collisionKey);
//                object.remove(Long.toString(iid));
                break;
            case READ:
                object.get(collisionKey);
//                object.get(Long.toString(iid));
                break;
        }
    }
}
