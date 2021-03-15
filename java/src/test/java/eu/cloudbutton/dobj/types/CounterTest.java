package eu.cloudbutton.dobj.types;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class CounterTest {

    private CounterFactory factory;

    @BeforeEach
    public void setUp(){
        factory = new CounterFactory();
    }

    @Test
    public void increment() throws ExecutionException, InterruptedException {
        doIncrement(factory.createdegradablecounter());
        doIncrement(factory.createjavacounter());
    }

    private static void doIncrement(Counter count) throws ExecutionException, InterruptedException {
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