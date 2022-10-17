package eu.cloudbutton.dobj.types;

import eu.cloudbutton.dobj.Factory;
import eu.cloudbutton.dobj.asymmetric.QueueMASP;
import eu.cloudbutton.dobj.incrementonly.BoxedLong;
import org.openjdk.jol.info.ClassLayout;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.*;

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
        factory.setFactoryQueue(cls);
        doConcurrentAppend(factory.getQueue());

        cls = Class.forName("eu.cloudbutton.dobj.asymmetric.QueueSASP");
        factory.setFactoryQueue(cls);
        doAppend(factory.getQueue());
    }

    private static void doAppend(Queue<Integer> queue){

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