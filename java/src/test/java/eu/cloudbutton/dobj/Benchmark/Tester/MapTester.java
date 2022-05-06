package eu.cloudbutton.dobj.Benchmark.Tester;

import java.util.AbstractMap;
import java.util.concurrent.CountDownLatch;

public class MapTester extends Tester<AbstractMap> {

    public MapTester(AbstractMap object, int[] ratios, CountDownLatch latch) {
        super(object, ratios, latch);
    }

    @Override
    protected long test(opType type) {

        long startTime = 0L, endTime = 0L;

        int rand = random.nextInt(ITEM_PER_THREAD);
        long iid = Thread.currentThread().getId() * 1_000_000_000L + rand;
//        CollisionKey collisionKey = new CollisionKey(Long.toString(iid));

        switch (type) {
            case ADD:
                startTime = System.nanoTime();
//                object.put(collisionKey, Long.toString(iid));
                endTime = System.nanoTime();
                object.put(Long.toString(iid), Long.toString(iid));
                break;
            case REMOVE:
                startTime = System.nanoTime();
//                object.remove(collisionKey);
                endTime = System.nanoTime();
                object.remove(Long.toString(iid));
                break;
            case READ:
                startTime = System.nanoTime();
//                object.get(collisionKey);
                endTime = System.nanoTime();
                object.get(Long.toString(iid));
                break;
        }

        return endTime - startTime;
    }
}
