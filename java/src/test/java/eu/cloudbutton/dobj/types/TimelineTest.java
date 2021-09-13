package eu.cloudbutton.dobj.types;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.AbstractList;
import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.concurrent.*;

import static org.testng.Assert.*;

public class TimelineTest {

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
        doAppend(factory.createDegradableLinkedList());
        doAppend(factory.createLinkedListSnapshot());
        doAppend(factory.createLinkedList());
        doAppend(factory.createSecondDegradableList());
        doAppend(factory.createThirdDegradableList());
    }

    private static void doAppend(AbstractQueue list) throws ExecutionException, InterruptedException {

        Timeline timeline = new Timeline(list, new Counter());

        ExecutorService executor = Executors.newFixedThreadPool(3);
        AbstractList<Future<Void>> futures = new ArrayList<>();
        Callable<Void> callable = () -> {
            timeline.add(1);
            timeline.add(2);
            timeline.add(3);
            return null;
        };

        timeline.add(4);
        timeline.add(4);
        timeline.add(4);
        for (int i = 0; i < 10; i++) {
            futures.add(executor.submit(callable));
        }

        for (Future<Void> future :futures){
            future.get();
        }

        System.out.println(timeline.read());

    }
}