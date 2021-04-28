package eu.cloudbutton.dobj.types;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.*;

class SetTest {

    private Factory factory;

    @BeforeTest
    void setUp() {
        factory = new Factory();
    }

    @Test
    void add() throws ExecutionException, InterruptedException {
        doAdd(factory.createDegradableSet());
        doAdd(factory.createSet());
    }

    private static void doAdd(AbstractSet set) throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        List<Future<Void>> futures = new ArrayList<>();
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

        assertEquals(result, set.read(), "Failed adding elements in the set");
    }
}