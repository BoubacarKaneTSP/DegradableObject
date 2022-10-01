package eu.cloudbutton.dobj.benchmark.Tester;

import eu.cloudbutton.dobj.benchmark.Microbenchmark;
import eu.cloudbutton.dobj.map.CollisionKeyFactory;
import eu.cloudbutton.dobj.map.PowerLawCollisionKey;
import eu.cloudbutton.dobj.benchmark.Microbenchmark.opType;

import java.lang.reflect.InvocationTargetException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class MapTester extends Tester<Map> {

    private boolean useCollisionKey;

    public MapTester(Map object, int[] ratios, CountDownLatch latch, boolean useCollisionKey) {
        super(object, ratios, latch);
        this.useCollisionKey = useCollisionKey;
    }

    @Override
    protected long test(opType type) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

        long startTime = 0L, endTime = 0L, nbFail = 0L;
        AbstractList list = new ArrayList<>();

        CollisionKeyFactory factory = new CollisionKeyFactory();

        factory.setFactoryCollisionKey(PowerLawCollisionKey.class);

        for (int i = 0; i < nbRepeat; i++) {
            int rand = random.nextInt(ITEM_PER_THREAD);
            String iid = Thread.currentThread().getId()  + Long.toString(rand);
//        long iid = Thread.currentThread().getId() * 1_000_000_000L + rand;

            if (useCollisionKey)
                list.add(factory.getCollisionKey());
            else
                list.add(iid);
        }


        switch (type) {
            case ADD:
                startTime = System.nanoTime();
                for (int i = 0; i < 2; i++) {
                    object.put(list.get(i), i);
                }
                endTime = System.nanoTime();
                break;
            case REMOVE:
                startTime = System.nanoTime();
                for (int i = 0; i < nbRepeat; i++) {
                    object.remove(list.get(i));
                }
                endTime = System.nanoTime();
                break;
            case READ:
                Object returnValue;
                startTime = System.nanoTime();
                for (int i = 0; i < 7; i++) {
                    returnValue = object.get(list.get(i));
                    if (returnValue == null)
                        nbFail++;
                }
                endTime = System.nanoTime();
                break;
        }

        return (endTime - startTime) / nbRepeat;
    }
}
