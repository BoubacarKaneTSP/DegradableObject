package eu.cloudbutton.dobj.types;

import eu.cloudbutton.dobj.Factory;
import eu.cloudbutton.dobj.key.Key;
import eu.cloudbutton.dobj.key.KeyGenerator;
import eu.cloudbutton.dobj.key.SimpleKeyGenerator;
import eu.cloudbutton.dobj.segmented.SegmentedHashSet;
import eu.cloudbutton.dobj.segmented.SegmentedSkipListSet;
import eu.cloudbutton.dobj.segmented.SegmentedTreeSet;
import eu.cloudbutton.dobj.set.ConcurrentHashSet;
import eu.cloudbutton.dobj.utils.Helpers;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SetTest {

    private static final int MAX_ITEMS_PER_THREAD = Integer.MAX_VALUE;
    private static final int ITEMS_PER_THREAD = 1_000;

    private Factory factory;
    private KeyGenerator generator;
    private static int parallelism;

    private static Class[] IMPL = {
            ConcurrentHashSet.class,
            SegmentedHashSet.class,
            SegmentedTreeSet.class,
            SegmentedSkipListSet.class
    };

    @BeforeTest
    void setUp() {
        factory = new Factory();
        generator = new SimpleKeyGenerator();
        parallelism = Runtime.getRuntime().availableProcessors();
    }

    @Test
    void test() throws Exception {
        Arrays.stream(IMPL).sequential().forEach(
                cls ->
                {
                    try {
                        factory.setFactorySet(cls);
                        doTest(factory.newSet());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }

    private void doTest(Set set) {
        Callable<Void> callable = () -> {
            try {
                List<Key> list = new ArrayList<>();
                for (int i = 0; i < ITEMS_PER_THREAD; i++) {
                    for(;;) {if (list.add(generator.nextKey())) break;}
                }
                list.stream().forEach(x -> set.add(x));
                assertEquals(set.containsAll(list), true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        };
        Helpers.executeAll(parallelism, callable);

        AtomicInteger nbElement = new AtomicInteger();
        set.stream().forEach(x -> nbElement.getAndIncrement());
        assertEquals(nbElement.get() == ITEMS_PER_THREAD * parallelism,true);
        assertEquals(nbElement.get(), set.size());

        callable = () -> {
            try {
                List<Key> list = new ArrayList<>();
                for (int i = 0; i < ITEMS_PER_THREAD; i++) {
                    for(;;) { if (list.add(generator.nextKey())) break;}
                }
                for (int i = 0; i < ITEMS_PER_THREAD; i++) {
                    set.remove(list.get(i));
                    assertEquals(set.contains(list.get(i)), false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        };
        Helpers.executeAll(parallelism, callable);
        assertEquals(set.size(), ITEMS_PER_THREAD * parallelism);

        set.clear();
        assertEquals(set.size(), 0);
    }

}