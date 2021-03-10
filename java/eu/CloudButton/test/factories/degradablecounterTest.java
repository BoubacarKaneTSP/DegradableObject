package factories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

class degradablecounterTest {

    private counter count;

    @BeforeEach
    public void setUp(){

        counterfactory counterFactory = new degradablecounterfactory();
        count = null;
        count = counterFactory.getcounter();
    }

    @Test
    void increment() throws ExecutionException, InterruptedException {
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
        assertEquals(10, count.read(), "Failed incrementing the counter");
    }

}