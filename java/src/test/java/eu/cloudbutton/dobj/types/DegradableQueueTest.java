package eu.cloudbutton.dobj.types;

import eu.cloudbutton.dobj.Factory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;

public class DegradableQueueTest {

    private Factory factory;

    @BeforeTest
    void setUp() {
        factory = new Factory();
    }

    @Test
    void offer() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, ExecutionException, InterruptedException {
        Class cls = Class.forName("eu.cloudbutton.dobj.asymmetric.QueueSASP");
        factory.setFactoryQueue(cls);
        doOffer(factory.newQueue());
    }

    private static void doOffer(Queue<Integer> queue) throws ExecutionException, InterruptedException {

        ExecutorService executor = Executors.newFixedThreadPool(3);
        List<Future<Void>> futures = new ArrayList<>();

        Callable<Void> callable = () -> {
            for (int i = 0; i < 100; i++) {
                queue.offer(i);
                if (Thread.currentThread().getName().equals("pool-1-thread-1"))
                    queue.poll();
            }
            return null;
        };


        for (int i = 0; i < 10; i++) {
            futures.add(executor.submit(callable));
        }

        for (Future<Void> future : futures) {
            future.get();
        }

    }
}