package eu.cloudbutton.dobj.types;

import eu.cloudbutton.dobj.Factory;
import eu.cloudbutton.dobj.asymmetric.QueueMASP;
import eu.cloudbutton.dobj.asymmetric.QueueSASP;
import eu.cloudbutton.dobj.incrementonly.BoxedLong;
import eu.cloudbutton.dobj.queue.WaitFreeQueue;
import org.openjdk.jol.info.ClassLayout;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.*;

import static org.testng.Assert.assertEquals;

public class QueueTest {

    private Factory factory;

    @BeforeTest
    void setUp() {
        factory = new Factory();
    }

    @Test
    void append() throws ExecutionException, InterruptedException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        Factory factory = new Factory();

        Class cls;

        cls = Class.forName("eu.cloudbutton.dobj.asymmetric.QueueMASP");
//        cls = Class.forName("java.util.concurrent.ConcurrentLinkedQueue");
        factory.setFactoryQueue(cls);
//        doConcurrentAppend(factory.getQueue());
        doAppend((Queue<Integer>) factory.getQueue());

//        cls = Class.forName("eu.cloudbutton.dobj.asymmetric.QueueSASP");
//        factory.setFactoryQueue(cls);
//        doAppend(factory.getQueue());

        cls = Class.forName("eu.cloudbutton.dobj.queue.WaitFreeQueue");
        factory.setFactoryQueue(cls);
        doEnqueue((WaitFreeQueue<Integer>) factory.getQueue());
        doSize((WaitFreeQueue<Integer>) factory.getQueue());

    }

    private static void doAppend(Queue<Integer> queue){

//        queue = ((ConcurrentLinkedQueue) queue);
        for (int i = 0; i < 1000; i++) {

            queue.poll();
            queue.offer(i);
            queue.offer(i);
            queue.offer(i);
        }
    }

    private static void doEnqueue(WaitFreeQueue<Integer> queue){
        queue.initRingTail();
        WaitFreeQueue.Handle<Integer> h =  queue.register();

        queue.enqueue(1, h);
        queue.enqueue(2, h);
        queue.enqueue(3, h);

        assertEquals(Integer.compare(queue.dequeue(h), 1),0);
        assertEquals(Integer.compare(queue.dequeue(h), 2),0);
        assertEquals(Integer.compare(queue.dequeue(h), 3),0);
    }

    private static void doSize(WaitFreeQueue<Integer> queue){
        queue.initRingTail();
        WaitFreeQueue.Handle<Integer> h =  queue.register();

        int size = 100;

        for (int i = 0; i < size; i++) {
            queue.enqueue(i, h);
        }

        assertEquals(Integer.compare(queue.size(), size),0);
    }

    private static void doConcurrentAppend(Queue<Integer> queue) throws ExecutionException, InterruptedException {
        /*ExecutorService executor = Executors.newFixedThreadPool(3);
        List<Future<Void>> futures = new ArrayList<>();
        Callable<Void> callable = () -> {
            queue.add(1);
            queue.add(2);
            queue.add(3);
            return null;
        };

        for (int i = 0; i < 10; i++) {
            futures.add(executor.submit(callable));
        }

        for (Future<Void> future :futures){
            future.get();
        }

        queue.poll();
        queue.add(1);
        queue.add(2);
        queue.add(3);

        *//*Iterator<Integer> it = list.iterator();
        int i = 0;

        while (it.hasNext()) {
            System.out.println(it.next());
            i++;
        }*/

    }
}