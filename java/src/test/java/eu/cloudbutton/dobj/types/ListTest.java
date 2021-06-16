package eu.cloudbutton.dobj.types;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.concurrent.*;

public class ListTest {

    private Factory factory;

    @BeforeTest
    void setUp() {
        factory = new Factory();
    }

    @Test
    void append() throws ExecutionException, InterruptedException {
        doAppend(factory.createDegradableList());
        doAppend(factory.createList());
        doAppend(factory.createListSnapshot());
        doAppend(factory.createSecondDegradableList());
        doAppend(factory.createThirdDegradableList());
        doAppend(factory.createDegradableLinkedList());
        doAppend(factory.createLinkedList());
        doAppend(factory.createLinkedListSnapshot());
    }

    private static void doAppend(AbstractList list) throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        java.util.List<Future<Void>> futures = new ArrayList<>();
        Callable<Void> callable = () -> {
            list.append(1);
            list.append(2);
            list.append(3);
            return null;
        };

        for (int i = 0; i < 10; i++) {
            futures.add(executor.submit(callable));
        }

        for (Future<Void> future :futures){
            future.get();
        }

        java.util.List<Integer> result = new ArrayList<>();
        result.add(1);
        result.add(2);
        result.add(3);
        assertTrue(list.read().containsAll(result), "Failed adding elements in the list");

        assertTrue(list.contains(3), "error in contains methods");
    }
}