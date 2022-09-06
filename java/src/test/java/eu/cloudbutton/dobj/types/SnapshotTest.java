package eu.cloudbutton.dobj.types;

import eu.cloudbutton.dobj.counter.Counter;
import eu.cloudbutton.dobj.Factory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.*;
import java.util.AbstractList;
import java.util.AbstractSet;
import java.util.List;
import java.util.concurrent.*;

import static org.testng.Assert.assertEquals;

public class SnapshotTest {

    private Factory factory;

    @BeforeTest
    void setUp() {
        factory = new Factory();
    }

    @Test
    public void test() throws ExecutionException, InterruptedException {
/*
        doCounterTest(factory
                .counter(new CounterSnapshot())
                .build()
                .getCounter()
        );

        doCounterTest(factory
                .counter(new CounterSnapshotSRMW())
                .build()
                .getCounter()
        );

        doSetTest(factory
                .set(new SetSnapshot())
                .build()
                .getSet()
        );

        doSetTest(factory
                .set(new SetSnapshotSRMW())
                .build()
                .getSet()
        );*/

        //        doListTest(factory.createListSnapshot());
    }

    public void doCounterTest(Counter snap) throws ExecutionException, InterruptedException {

        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<Void>> futures = new ArrayList<>();

        Callable<Void> callable = () -> {
            snap.incrementAndGet();
//            System.out.println(snap.read());
            return null;
        };

        for (int i = 0; i < 100000; i++) {
            futures.add(executor.submit(callable));
        }

        for (Future<Void> future : futures) {
            future.get();
        }

        assertEquals(snap.read(), 100000,"Failed incrementing the Counter");
    }

    public void doSetTest(AbstractSet snap) throws ExecutionException, InterruptedException {

        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<Void>> futures = new ArrayList<>();
        Random random = new Random();

        Callable<Void> callable = () -> {
            for (int i = 0; i < 1000; i++) {
                TimeUnit.MILLISECONDS.sleep(random.nextInt(10));
                snap.add("v"+i);
            }
            snap.remove("v7");
            return null;
        };
        Runnable runnable = () -> {
            for (int i = 0; i < 10000; i++) {
                try{
                    TimeUnit.MILLISECONDS.sleep(random.nextInt(10));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                snap.contains(Integer.toString(i));
            }
        };

        Thread reader = new Thread(runnable);
        reader.start();
        for (int i = 0; i < 10; i++) {
            futures.add(executor.submit(callable));
        }

        for (Future<Void> future : futures) {
            future.get();
        }

        Set<String> result = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            result.add("v"+i);
        }
        result.remove("v7");

//        assertEquals(snap, result,"Failed adding into the SetSnapshot and/or DegradableSetSnapshot object");


    }

    public void doListTest(AbstractQueue snap) throws ExecutionException, InterruptedException {

        ExecutorService executor = Executors.newFixedThreadPool(1);
        List<Future<Void>> futures = new ArrayList<>();

        Callable<Void> callable = () -> {
            snap.add("v1");
            snap.add("v2");
            snap.add("v3");

            return null;
        };

        for (int i = 0; i < 10000; i++) {
            futures.add(executor.submit(callable));
        }

        for (Future<Void> future : futures) {
            future.get();
        }

        List<String> result = new ArrayList<>();
        result.add("v1");
        result.add("v2");
        result.add("v3");

        assertEquals(snap, result,"Failed adding into the SetSnapshot object");
    }

}