package eu.cloudbutton.dobj.types;

import eu.cloudbutton.dobj.Factory;
import eu.cloudbutton.dobj.key.Key;
import eu.cloudbutton.dobj.key.KeyGenerator;
import eu.cloudbutton.dobj.key.SimpleKeyGenerator;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.*;

public class SetTest {

    private Factory factory;
    private KeyGenerator generator;

    @BeforeTest
    void setUp() {
        factory = new Factory();
        generator = new SimpleKeyGenerator(1000);
    }

    @Test
    void add() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
       Class cls = Class.forName("eu.cloudbutton.dobj.swsr.SWSRSkipListSet");
       factory.setFactorySet(cls);
       doAdd(factory.getSet());

       cls = Class.forName("eu.cloudbutton.dobj.set.ConcurrentHashSet");
       factory.setFactorySet(cls);
       doAdd(factory.getSet());
    }

    @Test
    void remove() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class cls = Class.forName("eu.cloudbutton.dobj.swsr.SWSRSkipListSet");
        factory.setFactorySet(cls);
        doRemove(factory.getSet());
    }

    @Test
    void testIterator() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, ExecutionException, InterruptedException {
        Class cls = Class.forName("eu.cloudbutton.dobj.mcwmcr.SetReadIntensive");
        factory.setFactorySet(cls);
        doTestIterator(factory.getSet());
    }

    private void doRemove(Set set) {
        Key k = generator.nextKey();
        set.add(k);
        set.remove(k);
        assertEquals(set.contains(k),false);
        assertEquals(set.isEmpty(),true);
    }

    private void doAdd(Set set) {
        List<Key> list = new ArrayList<>();
        int nbItem = 10;
        for(int i=0; i<10; i++) { list.add(generator.nextKey()); }

        for (int i = 0; i < nbItem; i++) set.add(list.get(i));
        assertEquals(set.containsAll(list),true);

        for (int i = 0; i < nbItem; i++) set.remove(list.get(i));
        assertEquals(set.isEmpty(),true, set.toString());
    }

    private void doTestIterator(Set<String> set) throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        List<Future<Void>> futures = new ArrayList<>();
        Callable<Void> callable = () -> {
            String name = Thread.currentThread().getName();
            set.add(name +" : v1");
            set.add(name +" : v2");
            set.add(name +" : v3");
            set.add(name +" : v4");
            set.add(name +" : v5");
            set.add(name +" : v6");
            return null;
        };

        for (int i = 0; i < 2; i++) {
            futures.add(executor.submit(callable));
        }

        for (Future<Void> future : futures) {
            future.get();
        }
    }
}