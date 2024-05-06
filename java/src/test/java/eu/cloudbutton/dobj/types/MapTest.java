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
    private KeyGenerator generator;
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
        Callable<Void> callable = () -> {
            try {
                List<Key> list = new ArrayList<>();
                for (int i = 0; i < ITEMS_PER_THREAD; i++) {
                    for (; ; ) {
                        if (list.add(generator.nextKey())) break;
                    }
                }
                list.stream().forEach(x -> map.put(x,x));
                assertEquals(list.stream().allMatch(x -> map.get(x)==x), true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        };
        Helpers.executeAll(parallelism, callable);

        AtomicInteger nbElement = new AtomicInteger();
        map.keySet().stream().forEach(_ -> nbElement.getAndIncrement());
        assertEquals(nbElement.get() == ITEMS_PER_THREAD * parallelism,true);
        assertEquals(nbElement.get(), map.size());

        callable = () -> {
            try {
                List<Key> list = new ArrayList<>();
                for (int i = 0; i < ITEMS_PER_THREAD; i++) {
                    for (; ; ) {
                        if (list.add(generator.nextKey())) break;
                    }
                }
                for (int i = 0; i < ITEMS_PER_THREAD; i++) {
                    map.remove(list.get(i));
                    assertEquals(map.containsKey(list.get(i)), false);
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

        Set<Key> keys = new ConcurrentSkipListSet<>();
        callable = () -> {
            try {
                for (int i = 0; i < ITEMS_PER_THREAD; i++) {
                    Key k = generator.nextKey();
                    map.put(k,k);
                    keys.add(k);
                    keys.stream().forEach(x->assertTrue(map.containsKey(x)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        };
        Helpers.executeAll(parallelism, callable);

        Map<Key,Key> previous = new ConcurrentHashMap<>();
        callable = () -> {
            Key c=null, p=generator.nextKey();
            map.put(p,"");
            try {
                for (int i = 0; i < 100; i++) {
                    c = generator.nextKey();
                    map.put(c,"");
                    previous.put(c,p);
                    p = c;
                    previous.keySet().stream().
                            forEach(x->
                            {
                                Key y = previous.get(x);
                                assertTrue(map.get(x)!=null);
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
