package eu.cloudbutton.dobj.types;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

import static org.testng.Assert.assertEquals;

public class SnapshotTest {

    private Factory factory;

    @BeforeTest
    public void setUp(){ factory = new Factory(); }

    @Test
    public void test() throws ExecutionException, InterruptedException {

        doCounterTest(factory.createCounterSnapshot());
        doCounterTest(factory.createCounterSnapshotV2());
//        doSetTest(factory.createSetSnapshot());
//        doSetTest(factory.createSetSnapshotV2());

//        doListTest(factory.createListSnapshot());
    }

    public void doCounterTest(AbstractCounter snap) throws ExecutionException, InterruptedException {

        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<Void>> futures = new ArrayList<>();

        Callable<Void> callable = () -> {
            snap.write();
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

                snap.read();
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

        java.util.Set result = new HashSet();
        for (int i = 0; i < 1000; i++) {
            result.add("v"+i);
        }
        result.remove("v7");

        assertEquals(snap.read(), result,"Failed adding into the SetSnapshot and/or DegradableSetSnapshot object");


    }

    public void doListTest(AbstractList snap) throws ExecutionException, InterruptedException {

        ExecutorService executor = Executors.newFixedThreadPool(1);
        List<Future<Void>> futures = new ArrayList<>();

        Callable<Void> callable = () -> {
            snap.append("v1");
            snap.append("v2");
            snap.append("v3");

            return null;
        };

        for (int i = 0; i < 10000; i++) {
            futures.add(executor.submit(callable));
        }

        for (Future<Void> future : futures) {
            future.get();
        }

        eu.cloudbutton.dobj.types.List result = new eu.cloudbutton.dobj.types.List();
        result.append("v1");
        result.append("v2");
        result.append("v3");

        assertEquals(snap.read(), result.read(),"Failed adding into the SetSnapshot object");
    }

}