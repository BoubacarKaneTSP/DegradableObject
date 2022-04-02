package eu.cloudbutton.dobj.Benchmark.Tester;

import eu.cloudbutton.dobj.types.CollisionKey;

import java.util.AbstractMap;
import java.util.concurrent.CountDownLatch;

public class MapTester extends Tester<AbstractMap> {

    public MapTester(AbstractMap object, int[] ratios, CountDownLatch latch) {
        super(object, ratios, latch);
    }

    @Override
    protected void test(opType type, long iid) {

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
