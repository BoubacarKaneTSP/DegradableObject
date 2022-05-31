package eu.cloudbutton.dobj.Benchmark.Tester;

import eu.cloudbutton.dobj.map.CollisionKey;

import java.util.AbstractMap;
import java.util.concurrent.CountDownLatch;

public class MapTester extends Tester<AbstractMap> {

    private final boolean collisionKey;

    public MapTester(AbstractMap object, int[] ratios, CountDownLatch latch, boolean collisionKey) {
        super(object, ratios, latch);
        this.collisionKey = collisionKey;
    }

    @Override
    protected long test(opType type) {

        long startTime = 0L, endTime = 0L;

        int rand = random.nextInt(ITEM_PER_THREAD);
        long iid = Thread.currentThread().getId() * 1_000_000_000L + rand;

        CollisionKey colKey = null;
        if (collisionKey)
            colKey = new CollisionKey(Long.toString(iid));


        switch (type) {
            case ADD:
                startTime = System.nanoTime();
                if (collisionKey)
                    object.put(colKey, Long.toString(iid));
                else
                    object.put(Long.toString(iid), Long.toString(iid));
                endTime = System.nanoTime();
                break;
            case REMOVE:
                startTime = System.nanoTime();
                if (collisionKey)
                    object.remove(colKey);
                else
                    object.remove(Long.toString(iid));
                endTime = System.nanoTime();
                break;
            case READ:
                startTime = System.nanoTime();
                if (collisionKey)
                    object.get(colKey);
                else
                    object.get(Long.toString(iid));
                endTime = System.nanoTime();
                break;
        }

        return endTime - startTime;
    }
}
