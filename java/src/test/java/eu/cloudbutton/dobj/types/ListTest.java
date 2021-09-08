package eu.cloudbutton.dobj.types;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import static org.testng.Assert.assertTrue;

import java.util.AbstractList;
import java.util.AbstractQueue;
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
        doAppend(factory.createList());
        doAppend(factory.createDegradableList());
        doAppend(factory.createListSnapshot());
        doAppend(factory.createSecondDegradableList());
        doAppend(factory.createThirdDegradableList());
        doAppend(factory.createDegradableLinkedList());
        doAppend(factory.createLinkedList());
        doAppend(factory.createLinkedListSnapshot());
    }

    private static void doAppend(AbstractQueue list) throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        AbstractList<Future<Void>> futures = new ArrayList<>();
        Callable<Void> callable = () -> {
            list.add(1);
            list.add(2);
            list.add(3);
            return null;
        };

        for (int i = 0; i < 10; i++) {
            futures.add(executor.submit(callable));
        }

        for (Future<Void> future :futures){
            future.get();
        }

        AbstractList<Integer> result = new ArrayList<>();
        result.add(1);
        result.add(2);
        result.add(3);
        assertTrue(list.contains(1), "Failed adding elements in the list");
        assertTrue(list.contains(2), "Failed adding elements in the list");
        assertTrue(list.contains(3), "Failed adding elements in the list");
    }
}