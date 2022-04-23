package eu.cloudbutton.dobj.types;

import eu.cloudbutton.dobj.Factory;
import eu.cloudbutton.dobj.Set.DegradableSet;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.util.AbstractList;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.*;

public class SetTest {

    private Factory factory;

    @BeforeTest
    void setUp() {
        factory = new Factory();
    }

    @Test
    void add() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, ExecutionException, InterruptedException {
       Class cls = Class.forName("eu.cloudbutton.dobj.Set.DegradableSet");
       factory.setFactorySet(cls);
       doAdd(factory.getSet());
    }

    @Test
    void testIterator() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, ExecutionException, InterruptedException {
        Class cls = Class.forName("eu.cloudbutton.dobj.Set.DegradableSet");
        factory.setFactorySet(cls);
        doTestIterator(factory.getSet());
    }

    private static void doAdd(AbstractSet set) throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        AbstractList<Future<Void>> futures = new ArrayList<>();
        Callable<Void> callable = () -> {
            set.add("v1");
            set.add("v2");
            set.add("v3");
            return null;
        };

        for (int i = 0; i < 10; i++) {
            futures.add(executor.submit(callable));
        }

        for (Future<Void> future :futures){
            future.get();
        }

        java.util.Set<String> result = new HashSet<>();
        result.add("v1");
        result.add("v2");
        result.add("v3");

        assertEquals(set.contains("v1"), true,"error in contains methods");
        assertEquals(set.contains("v2"), true,"error in contains methods");
        assertEquals(set.contains("v3"), true,"error in contains methods");
    }

    private static void doTestIterator(AbstractSet<String> set) throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        AbstractList<Future<Void>> futures = new ArrayList<>();
        Callable<Void> callable = () -> {
            set.add(Thread.currentThread().getName()+" : v1");
            set.add(Thread.currentThread().getName()+" : v2");
            set.add(Thread.currentThread().getName()+" : v3");
            set.add(Thread.currentThread().getName()+" : v4");
            set.add(Thread.currentThread().getName()+" : v5");
            set.add(Thread.currentThread().getName()+" : v6");
            return null;
        };

        for (int i = 0; i < 10; i++) {
            futures.add(executor.submit(callable));
        }

        for (Future<Void> future : futures) {
            future.get();
        }

        for (String s: set){
            System.out.println(s);
        }
    }
}