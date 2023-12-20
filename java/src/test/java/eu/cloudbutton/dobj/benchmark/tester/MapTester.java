package eu.cloudbutton.dobj.benchmark.tester;

import eu.cloudbutton.dobj.incrementonly.BoxedLong;
import eu.cloudbutton.dobj.key.Key;
import eu.cloudbutton.dobj.key.KeyGenerator;
import eu.cloudbutton.dobj.key.RetwisKeyGenerator;
import eu.cloudbutton.dobj.benchmark.Microbenchmark.opType;
import eu.cloudbutton.dobj.key.SimpleKeyGenerator;

import java.lang.reflect.InvocationTargetException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class MapTester extends Tester<Map> {

    private KeyGenerator keyGenerator;
    Key user;
    public MapTester(Map<Key, Integer> object, int[] ratios, CountDownLatch latch, boolean useCollisionKey, int max_item_per_thread) {
        super(object, ratios, latch);
        keyGenerator = useCollisionKey ? new RetwisKeyGenerator(max_item_per_thread) : new SimpleKeyGenerator(max_item_per_thread);
//        user = keyGenerator.nextKey();
    }

    @Override
    protected long test(opType type) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

        long startTime = 0L, endTime = 0L;
        List list = new ArrayList<>();
        for (int i = 0; i < nbRepeat; i++) {
            list.add(keyGenerator.nextKey());
        }

        switch (type) {
            case ADD:
                startTime = System.nanoTime();
                for (int i = 0; i < nbRepeat; i++) {
                    int finalI = i;
                    object.compute(list.get(0), (k, v) -> finalI);
//                    object.compute(user, (k, v) -> finalI);
                }
                endTime = System.nanoTime();
//                object.put(list.get(i), i);
                break;
            case REMOVE:
                startTime = System.nanoTime();
                for (int i = 0; i < nbRepeat; i++) {
                    object.remove(list.get(i));
                }
                endTime = System.nanoTime();
                break;
            case READ:
                for (int i = 0; i < nbRepeat; i++) {
                    object.put(list.get(i),i);
                }
                startTime = System.nanoTime();
                for (int i = 0; i < nbRepeat; i++) {
                    object.get(list.get(i));
                }
                endTime = System.nanoTime();
                break;
        }

        return (endTime - startTime);
    }

    @Override
    protected long test(opType type, BoxedLong boxedLong) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        return 0;
    }
}
