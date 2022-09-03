package eu.cloudbutton.dobj.types;

import eu.cloudbutton.dobj.Factory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.*;
import java.util.concurrent.*;

public class QueueTest {

    private Factory factory;

    @BeforeTest
    void setUp() {
        factory = new Factory();
    }

    @Test
    void append() throws ExecutionException, InterruptedException {
       /* doAppend(factory.createList());
        doAppend(factory.createDegradableList());
        doAppend(factory.createListSnapshot());
        doAppend(factory.createDegradableLinkedList());
        doAppend(factory.createLinkedList());
        doAppend(factory.createLinkedListSnapshot());*/
    }

    private static void doAppend(AbstractQueue<Integer> list) throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        List<Future<Void>> futures = new ArrayList<>();
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

        list.poll();
        list.add(1);
        list.add(2);
        list.add(3);

        Iterator<Integer> it = list.iterator();
        int i = 0;

        while (it.hasNext()) {
            System.out.println(it.next());
            i++;
        }

        System.out.println("size : " + i);
        /*
        assertTrue(list.contains(1), "Failed adding elements in the list");
        assertTrue(list.contains(2), "Failed adding elements in the list");
        assertTrue(list.contains(3), "Failed adding elements in the list");
        */
    }
}