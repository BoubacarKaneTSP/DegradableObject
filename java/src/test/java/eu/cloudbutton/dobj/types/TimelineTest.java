package eu.cloudbutton.dobj.types;

import eu.cloudbutton.dobj.Factory;
import org.javatuples.Pair;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.*;
import java.util.concurrent.*;

public class TimelineTest {

    private Factory factory;

    @BeforeTest
    void setUp() {
        factory = new Factory();
    }


    @Test
    void append() throws ExecutionException, InterruptedException {

        Pair<Integer, Integer> pair1 = new Pair<>(1,2);
        Pair<Integer, Integer> pair2 = new Pair<>(1,2);

        System.out.println(pair1.equals(pair2));
//        for (int i = 0; i < 1600000000; i++) {
//            someMethods();
////            someOtherMethods();
//        }

        /*
        Class cls = Class.forName("ConcurrentLinkedQueue");
        doAppend(factory.getQueue());

        doAppend(factory
                .queue(new MapQueue())
                .build()
                .getQueue()
        );*/
    }

    private void someMethods() throws InterruptedException {
        for (int i = 0; i < 2000; i++) {
            Thread.sleep(1);
        }
        return;
    }

    private void someOtherMethods() throws InterruptedException {
        for (int i = 0; i < 3000; i++) {
            Thread.sleep(1);
//            someOtherMethods2();
        }
    }

    private void someOtherMethods2() throws InterruptedException {
        for (int i = 0; i < 3000; i++) {
            Thread.sleep(1);
        }
    }

    private static void doAppend(AbstractQueue list) throws ExecutionException, InterruptedException {

//        Timeline timeline = new Timeline(list, new DegradableCounter());

/*        ExecutorService executor = Executors.newFixedThreadPool(3);
        List<Future<Void>> futures = new ArrayList<>();
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
        System.out.println();*/
    }
}