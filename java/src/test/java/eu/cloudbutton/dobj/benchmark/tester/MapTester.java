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

import static java.lang.Thread.*;

public class MapTester extends Tester<Map> {

    private KeyGenerator keyGenerator;
    private List<Key> list;
    private long startTime, endTime;

    public MapTester(Map<Key, Integer> object, int[] ratios, CountDownLatch latch, boolean useCollisionKey, int max_item_per_thread) {
        super(object, ratios, latch);
        keyGenerator = useCollisionKey ? new RetwisKeyGenerator(max_item_per_thread) : new SimpleKeyGenerator(max_item_per_thread);
        list = new ArrayList<>();
    }

    @Override
    protected long test(opType type) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        startTime = 0L;
        endTime = 0L;

        list.clear();
        for (int i = 0; i < nbRepeat; i++) {
            list.add(keyGenerator.nextKey());
        }

        switch (type) {
            case ADD:
                startTime = System.nanoTime();
                for (int i = 0; i < nbRepeat; i++) {object.put(list.get(i),i);
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
                startTime = System.nanoTime();
                for (int i = 0; i < nbRepeat; i++) {
                    // object.get(list.get(i));
                    int finalI = i;
                    object.compute(list.get(i), (k, v) -> {
                        try {
                            sleep(0,5000);
                            return finalI;
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
                endTime = System.nanoTime();
                break;
        }
        return (endTime - startTime);
    }

}
