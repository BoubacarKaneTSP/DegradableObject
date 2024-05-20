package eu.cloudbutton.dobj.types;

import eu.cloudbutton.dobj.Factory;
import eu.cloudbutton.dobj.asymmetric.swmr.map.SWMRHashMap;
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
    private static final int ITEMS_PER_THREAD = 100_000;

    private Factory factory;
    private SimpleKeyGenerator generator;
    private static int parallelism;
    private ExecutorService executorService;

    private static Class[] IMPL = {
//            ExtendedSegmentedHashMap.class,
            eu.cloudbutton.dobj.juc.ConcurrentHashMap.class,
//            SegmentedHashMap.class,
//            SegmentedTreeMap.class,
//            SegmentedSkipListMap.class,
    };

    @BeforeTest
    void setUp() {
        factory = new Factory();
        generator = new SimpleKeyGenerator();
        parallelism = Runtime.getRuntime().availableProcessors();
        executorService = Executors.newFixedThreadPool(parallelism);
    }

    void executeAll(Callable<Void> callable) {
        try {
            List<Future<Void>> futures = new ArrayList<>();
            for (int i = 0; i < parallelism; i++) {
                futures.add(executorService.submit(callable));
            }
            for (Future<Void> future : futures) {
                future.get();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
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

        System.out.println("Each thread can see what it has added");
        Map<Integer, List<Key>> keys = generator.generateAndSplit(
                ITEMS_PER_THREAD * parallelism, parallelism);
        CountDownLatch latch = new CountDownLatch(parallelism);

        Callable<Void> callable = () -> {
            try {
                latch.countDown();
                latch.await();
                Collection<Key> collection = keys.get(Helpers.threadIndexInPool());
                collection.stream().forEach(x -> map.put(x,x));
                assertEquals(collection.stream().allMatch(x -> map.get(x).equals(x)), true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        };
        executeAll(callable);
        System.out.println("Done");

//        AtomicInteger nbElement = new AtomicInteger();
//        map.keySet().stream().forEach(_ -> nbElement.getAndIncrement());
//        assertEquals(nbElement.get(), ITEMS_PER_THREAD * parallelism, "All elements are not successfully added");
//        assertEquals(nbElement.get(), map.size(), "Error with map.size()");
//
//        System.out.println("Test remove");
//        Map<Integer, List<Key>> keys2 = generator.generateAndSplit(
//                ITEMS_PER_THREAD*parallelism, parallelism);
//        CountDownLatch latch2 = new CountDownLatch(parallelism);
//        callable = () -> {
//            try {
//                latch2.countDown();
//                latch2.await();
//                Collection<Key> collection = keys2.get(Helpers.threadIndexInPool());
//                for (Key key : collection) {
//                    map.remove(key);
//                    assertEquals(map.containsKey(key), false);
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            return null;
//        };
//        executeAll(callable);
//        System.out.println("Done");
//
//        assertEquals(map.size(), ITEMS_PER_THREAD * parallelism); // Should be false ?
//        map.clear();
//        assertEquals(map.size(), 0);
//
//        System.out.println("Each thread can see what other thread has added");
//        Map<Integer, List<Key>> keys3 = generator.generateAndSplit(
//                ITEMS_PER_THREAD * parallelism / 50, parallelism);
//
//        Set<Key> other = new ConcurrentSkipListSet<>();
//        CountDownLatch latch3 = new CountDownLatch(parallelism);
//        callable = () -> {
//            try {
//                latch3.countDown();
//                latch3.await();
//                Collection<Key> collection = keys3.get(Helpers.threadIndexInPool());
//                for (Key key : collection) {
//                    map.put(key,key);
//                    other.add(key);
//                    other.stream().forEach(x->assertTrue(map.containsKey(x)));
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            return null;
//        };
//        executeAll(callable);
//        System.out.println("Done");
//
//        System.out.println("Check if previous values are added");
//        Map<Key,Key> previous = new ConcurrentHashMap<>();
//        Map<Integer, List<Key>> keys4 = generator.generateAndSplit(
//                100 * parallelism, parallelism);
//
//        CountDownLatch latch4 = new CountDownLatch(parallelism);
//        callable = () -> {
//            latch4.countDown();
//            latch4.await();
//            List<Key> collection = keys4.get(Helpers.threadIndexInPool());
//            Key c, p = collection.get(0);
//            map.put(p,"");
//            try {
//                for (int i = 1; i < collection.size(); i++) {
//                    c = collection.get(i);
//                    map.put(c,"");
//                    previous.put(c,p);
//                    p = c;
//                    previous.keySet().stream().
//                            forEach(x->
//                            {
//                                Key y = previous.get(x);
//                                assertTrue(map.containsKey(y));
//                            });
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            return null;
//        };
//        executeAll(callable);
//        System.out.println("done");

    }
}
