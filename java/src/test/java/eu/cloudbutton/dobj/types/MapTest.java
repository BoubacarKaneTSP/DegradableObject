package eu.cloudbutton.dobj.types;

import eu.cloudbutton.dobj.Factory;
import eu.cloudbutton.dobj.key.Key;
import eu.cloudbutton.dobj.key.KeyGenerator;
import eu.cloudbutton.dobj.key.SimpleKeyGenerator;
import eu.cloudbutton.dobj.segmented.ExtendedSegmentedHashMap;
import eu.cloudbutton.dobj.segmented.SegmentedHashMap;
import eu.cloudbutton.dobj.utils.Helpers;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.testng.Assert.*;

public class MapTest {
    private static final int MAX_ITEMS_PER_THREAD = Integer.MAX_VALUE;
    private static final int ITEMS_PER_THREAD = 1_000;

    private Factory factory;
    private SimpleKeyGenerator generator;
    private static int parallelism;

    private static Class[] IMPL = {
            ConcurrentHashMap.class,
            SegmentedHashMap.class,
//            SegmentedTreeMap.class,
//            SegmentedSkipListMap.class,
            ExtendedSegmentedHashMap.class
    };

    @BeforeTest
    void setUp() {
        factory = new Factory();
        generator = new SimpleKeyGenerator(MAX_ITEMS_PER_THREAD);
        parallelism = Runtime.getRuntime().availableProcessors();
    }

    @Test
    void test() throws Exception {
        Arrays.stream(IMPL).sequential().forEach(
                cls ->
                {
                    try {
                        factory.setFactoryMap(cls);
                        long startTime = System.nanoTime();
                        doTest(factory.newMap());
                        long endTime = System.nanoTime() - startTime;
                        System.out.println(cls.getSimpleName()+": " + (endTime / 1_000_000) + " ms");
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }

    private void doTest(Map map) {

        Map<Integer, List<Key>> keys = generator.generateAndSplit(
                ITEMS_PER_THREAD * parallelism, parallelism);
        CountDownLatch latch = new CountDownLatch(parallelism);
        Callable<Void> callable = () -> {
            try {
                latch.countDown();
                latch.await();
                int me = Helpers.threadIndexInPool();
                Collection<Key> collection = keys.get(Helpers.threadIndexInPool());
                collection.stream().forEach(x -> map.put(x,x));
                assertEquals(collection.stream().allMatch(x -> map.get(x)==x), true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        };
        Helpers.executeAll(parallelism, callable);
        AtomicInteger nbElement = new AtomicInteger();
        map.keySet().stream().forEach(_ -> nbElement.getAndIncrement());
        assertEquals(nbElement.get(), ITEMS_PER_THREAD * parallelism);
        assertEquals(nbElement.get(), map.size());

        Map<Integer, List<Key>> keys2 = generator.generateAndSplit(
                ITEMS_PER_THREAD*parallelism, parallelism);
        CountDownLatch latch2 = new CountDownLatch(parallelism);
        callable = () -> {
            try {
                latch2.countDown();
                latch2.await();
                Collection<Key> collection = keys2.get(Helpers.threadIndexInPool());
                for (Key key : collection) {
                    map.remove(key);
                    assertEquals(map.containsKey(key), false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        };
        Helpers.executeAll(parallelism, callable);
        assertEquals(map.size(), ITEMS_PER_THREAD * parallelism);
        map.clear();
        assertEquals(map.size(), 0);

        Set<Key> keys3 = new ConcurrentSkipListSet<>();
        CountDownLatch latch3 = new CountDownLatch(parallelism);
        callable = () -> {
            try {
                latch2.countDown();
                latch2.await();
                Collection<Key> collection = keys2.get(Helpers.threadIndexInPool());
                for (Key key : collection) {
                    map.put(key,key);
                    keys3.add(key);
                    keys3.stream().forEach(x->assertTrue(map.containsKey(x)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        };
        Helpers.executeAll(parallelism, callable);

        Map<Key,Key> previous = new ConcurrentHashMap<>();
        Map<Integer, List<Key>> keys4 = generator.generateAndSplit(
                100 * parallelism, parallelism);

        callable = () -> {
            List<Key> collection = keys4.get(Helpers.threadIndexInPool());
            Key c, p = collection.get(0);
            map.put(p,"");
            try {
                for (int i = 1; i < collection.size(); i++) {
                    c = collection.get(i);
                    map.put(c,"");
                    previous.put(c,p);
                    p = c;
                    previous.keySet().stream().
                            forEach(x->
                            {
                                Key y = previous.get(x);
                                assertTrue(map.containsKey(y));
                            });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        };
        Helpers.executeAll(parallelism, callable);

    }
}
