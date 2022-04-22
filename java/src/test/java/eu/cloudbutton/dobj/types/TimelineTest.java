package eu.cloudbutton.dobj.types;

import eu.cloudbutton.dobj.Counter.DegradableCounter;
import eu.cloudbutton.dobj.Factory;
import eu.cloudbutton.dobj.Queue.MapQueue;
import eu.cloudbutton.dobj.Timeline;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.*;
import java.util.AbstractList;
import java.util.concurrent.*;

public class TimelineTest {

    private Factory factory;

    @BeforeTest
    void setUp() {
        factory = new Factory();
    }


    @Test
    void append() throws ExecutionException, InterruptedException {
/*
        Class cls = Class.forName("ConcurrentLinkedQueue");
        doAppend(factory.getQueue());

        doAppend(factory
                .queue(new MapQueue())
                .build()
                .getQueue()
        );*/
    }

    private static void doAppend(AbstractQueue list) throws ExecutionException, InterruptedException {

        Timeline timeline = new Timeline(list, new DegradableCounter());

        ExecutorService executor = Executors.newFixedThreadPool(3);
        AbstractList<Future<Void>> futures = new ArrayList<>();
        Callable<Void> callable = () -> {
            timeline.add(1);
            timeline.add(2);
            timeline.add(3);
            timeline.add(4);
            timeline.add(5);
            return null;
        };

        for (int i = 0; i < 20; i++) {
            futures.add(executor.submit(callable));
        }

        for (Future<Void> future :futures){
            future.get();
        }


        timeline.add(10);
        timeline.add(11);
        timeline.add(12);

        System.out.println(timeline.read());
        System.out.println(timeline.read().size());
        System.out.println();
    }
}