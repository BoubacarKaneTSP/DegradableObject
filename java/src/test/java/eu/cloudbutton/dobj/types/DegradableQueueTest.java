package eu.cloudbutton.dobj.types;

import eu.cloudbutton.dobj.Factory;
import eu.cloudbutton.dobj.Queue.DegradableQueue;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.AbstractQueue;

public class DegradableQueueTest {

    private Factory factory;

    @BeforeTest
    void setUp() {
        factory = new Factory();
    }

    @Test
    void offer() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class cls = Class.forName("eu.cloudbutton.dobj.Queue.DegradableQueue");
        factory.setFactoryQueue(cls);
        doOffer(factory.getQueue());
    }

    private static void doOffer(AbstractQueue queue){

        queue.offer(1);
        queue.offer(2);
        queue.offer(3);
        queue.poll();
    }
}