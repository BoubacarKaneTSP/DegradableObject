package eu.cloudbutton.dobj.types;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class CounterTest {

    private Factory factory;

    @BeforeTest
    public void setUp(){
        factory = new Factory();
    }

    @Test
    public void increment() throws ExecutionException, InterruptedException {
        doIncrement(factory.createDegradableCounter());
        doIncrement(factory.createCounter());
    }

    private static void doIncrement(AbstractCounter count) throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        List<Future<Void>> futures = new ArrayList<>();
        Callable<Void> callable = () -> {
            count.increment();
            return null;
        };

        for (int i = 0; i < 10; i++) {
            futures.add(executor.submit(callable));
        }

        for (Future<Void> future : futures) {
            future.get();
        }
        assertEquals(10, count.read(),"Failed incrementing the Counter");
//        assertEquals("Failed incrementing the Counter",10, count.read());
    }

}