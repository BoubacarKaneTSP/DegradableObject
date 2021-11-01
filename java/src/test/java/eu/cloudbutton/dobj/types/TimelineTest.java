package eu.cloudbutton.dobj.types;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.*;
import java.util.AbstractList;
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
//        doAppend(factory.createList());
//        doAppend(factory.createDegradableList());
//        doAppend(factory.createListSnapshot());
//        doAppend(factory.createDegradableLinkedList());
//        doAppend(factory.createLinkedListSnapshot());
//        doAppend(factory.createLinkedList());
//        doAppend(factory.createSecondDegradableList());
//        doAppend(factory.createThirdDegradableList());
        doAppend(factory.createConcurrentLinkedDeque());
        doAppend(factory.createTimelineQueue());
    }

    private static void doAppend(AbstractCollection list) throws ExecutionException, InterruptedException {

        Timeline timeline = new Timeline(list, 50);

        ExecutorService executor = Executors.newFixedThreadPool(3);
        AbstractList<Future<Void>> futures = new ArrayList<>();
        Callable<Void> callable = () -> {
            timeline.add(1);
            timeline.add(2);
            timeline.add(3);
            return null;
        };

        for (int i = 0; i < 100; i++) {
            futures.add(executor.submit(callable));
        }

        for (Future<Void> future :futures){
            future.get();
        }

        timeline.add(4);
        timeline.add(4);
        timeline.add(4);

        System.out.println(timeline.read());
    }
}