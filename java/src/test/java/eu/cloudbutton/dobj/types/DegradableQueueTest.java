package eu.cloudbutton.dobj.types;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.AbstractQueue;

import static org.testng.Assert.*;

public class DegradableQueueTest {

    private Factory.FactoryBuilder factory;

    @BeforeTest
    void setUp() {
        factory = Factory.builder();
    }

    @Test
    void offer(){
        doOffer(factory
                .queue(new DegradableQueue())
                .build()
                .getQueue()
        );
    }

    private static void doOffer(AbstractQueue queue){

        queue.offer(1);
        queue.offer(2);
        queue.offer(3);
        queue.poll();
    }
}