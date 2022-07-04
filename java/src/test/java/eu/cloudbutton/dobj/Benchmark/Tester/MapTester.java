package eu.cloudbutton.dobj.Benchmark.Tester;

import eu.cloudbutton.dobj.map.CollisionKey;

import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import static eu.cloudbutton.dobj.Benchmark.Benchmark.collisionKey;

public class MapTester extends Tester<AbstractMap> {


    public MapTester(AbstractMap object, int[] ratios, CountDownLatch latch) {
        super(object, ratios, latch);
    }

    @Override
    protected long test(opType type) {

        long startTime = 0L, endTime = 0L;

        AbstractList list = new ArrayList<>();

        for (int i = 0; i < nbRepeat; i++) {
            int rand = random.nextInt(ITEM_PER_THREAD);
            String iid = Thread.currentThread().getId()  + Long.toString(rand);
//        long iid = Thread.currentThread().getId() * 1_000_000_000L + rand;

            if (collisionKey)
                list.add(new CollisionKey(iid));
            else
                list.add(iid);
        }


        switch (type) {
            case ADD:
                startTime = System.nanoTime();
                for (int i = 0; i < nbRepeat; i++) {
                    object.put(list.get(i), i);
                }
                endTime = System.nanoTime();
                for (int i = 0; i < nbRepeat; i++) {
                    object.remove(list.get(i));
                }
                break;
            case REMOVE:
                startTime = System.nanoTime();
                for (int i = 0; i < nbRepeat; i++) {
                    object.remove(list.get(i));
                }
                endTime = System.nanoTime();
                break;
            case READ:
                startTime = System.nanoTime();
                for (int i = 0; i < nbRepeat; i++) {
                    object.get(list.get(i));
                }
                endTime = System.nanoTime();
                break;
        }

        return (endTime - startTime) / nbRepeat;
    }
}
