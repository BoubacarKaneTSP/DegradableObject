package eu.cloudbutton.dobj.types;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.testng.Assert.assertEquals;

public class SnapshotTest {

    private Factory factory;

    @BeforeTest
    public void setUp(){ factory = new Factory(); }

    @Test
    public void test() throws ExecutionException, InterruptedException {
        doCounterTest(factory.createCounterSnapshot());
        doCounterTest(factory.createDegradableCounterSnapshot());

//        doSetTest(factory.createSetSnapshot());
//        doSetTest(factory.createDegradableSetSnapshot());

//        doListTest(factory.createListSnapshot());
//        doListTest(factory.createDegradableListSnapshot());
    }

    public void doCounterTest(AbstractCounterSnapshot snap) throws ExecutionException, InterruptedException {

        ExecutorService executor = Executors.newFixedThreadPool(1);
        List<Future<Void>> futures = new ArrayList<>();

        Callable<Void> callable = () -> {
            snap.write();
            return null;
        };

        for (int i = 0; i < 10000; i++) {
            futures.add(executor.submit(callable));
        }

        for (Future<Void> future : futures) {
            future.get();
        }

        assertEquals(snap.read(), 10000,"Failed incrementing the Counter");
    }

    public void doSetTest(AbstractSetSnapshot snap) throws ExecutionException, InterruptedException {

        ExecutorService executor = Executors.newFixedThreadPool(1);
        List<Future<Void>> futures = new ArrayList<>();

        Callable<Void> callable = () -> {
            snap.write("v1");
            snap.write("v2");
            snap.write("v3");

            return null;
        };

        for (int i = 0; i < 10000; i++) {
            futures.add(executor.submit(callable));
        }

        for (Future<Void> future : futures) {
            future.get();
        }

        Set result = new Set();
        result.add("v1");
        result.add("v2");
        result.add("v3");

        assertEquals(snap.read().read(), result.read(),"Failed adding into the SetSnapshot object");
    }

    public void doListTest(AbstractListSnapshot snap) throws ExecutionException, InterruptedException {

        ExecutorService executor = Executors.newFixedThreadPool(1);
        List<Future<Void>> futures = new ArrayList<>();

        Callable<Void> callable = () -> {
            snap.write("v1");
            snap.write("v2");
            snap.write("v3");

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

        assertEquals(snap.read().read(), result.read(),"Failed adding into the SetSnapshot object");
    }

}