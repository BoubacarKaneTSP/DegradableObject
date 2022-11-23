package eu.cloudbutton.dobj.types;

import eu.cloudbutton.dobj.Factory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.*;

public class SetTest {

    private Factory factory;

    @BeforeTest
    void setUp() {
        factory = new Factory();
    }

    @Test
    void add() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, ExecutionException, InterruptedException {
       Class cls = Class.forName("eu.cloudbutton.dobj.asymmetric.SetMWSR");
       factory.setFactorySet(cls);
       doAdd(factory.getSet());
    }

    @Test
    void testIterator() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, ExecutionException, InterruptedException {
        Class cls = Class.forName("eu.cloudbutton.dobj.mcwmcr.SetReadIntensive");
        factory.setFactorySet(cls);
        doTestIterator(factory.getSet());
    }

    private static void doAdd(Set set) throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        List<Future<Void>> futures = new ArrayList<>();
        Callable<Void> callable = () -> {
//            System.out.println(Thread.currentThread().getName());
            set.add(Thread.currentThread().getName());
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
        TimeUnit.SECONDS.sleep(1);


        set.size();

        TimeUnit.SECONDS.sleep(1);

        System.out.println(set);

        java.util.Set<String> result = new HashSet<>();
        result.add("v1");
        result.add("v2");
        result.add("v3");

        assertEquals(set.contains("v1"), true,"error in contains methods");
        assertEquals(set.contains("v2"), true,"error in contains methods");
        assertEquals(set.contains("v3"), true,"error in contains methods");
    }

    private static void doTestIterator(Set<String> set) throws ExecutionException, InterruptedException {
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