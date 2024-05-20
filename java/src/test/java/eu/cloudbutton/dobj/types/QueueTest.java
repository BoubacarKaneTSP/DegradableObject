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
    public void test() throws ExecutionException, InterruptedException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        doAppend(new QueueMASP<>());
    }

    private static void doAppend(Queue<Integer> queue){
        for (int i = 0; i < 1000; i++) {
            assert queue.offer(i) == true;
            assert queue.poll() == i;
            assert queue.isEmpty();
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