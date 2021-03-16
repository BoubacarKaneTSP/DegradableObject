package eu.cloudbutton.dobj.types;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.concurrent.*;

class ListTest {

    private ListFactory factory;

    @BeforeTest
    void setUp() {
        factory = new ListFactory();
    }

    @Test
    void append() throws ExecutionException, InterruptedException {
        doAppend(factory.createdegradablelist());
        doAppend(factory.createjavalist());
    }

    private static void doAppend(AbstractList list) throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        java.util.List<Future<Void>> futures = new ArrayList<>();
        Callable<Void> callable = () -> {
            list.append("v1");
            list.append("v2");
            list.append("v3");
            return null;
        };

        for (int i = 0; i < 10; i++) {
            futures.add(executor.submit(callable));
        }

        for (Future<Void> future :futures){
            future.get();
        }

        java.util.List<String> result = new ArrayList<>();
        result.add("v1");
        result.add("v2");
        result.add("v3");
        assertTrue(list.read().containsAll(result), "Failed adding elements in the list");
    }
}