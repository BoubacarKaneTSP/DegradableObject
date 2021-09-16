package eu.cloudbutton.dobj.types;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.AbstractQueue;

import static org.testng.Assert.*;

public class DegradableQueueTest {

    private Factory factory;

    @BeforeTest
    void setUp() {
        factory = new Factory();
    }

    @Test
    void offer(){
        doOffer(factory.createDegradableQueue());
    }

    private static void doOffer(AbstractQueue queue){

        queue.offer(1);
        queue.offer(2);
        queue.offer(3);
        queue.poll();
    }
}