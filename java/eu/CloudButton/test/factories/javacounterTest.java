package factories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

class javacounterTest {

    private counter count;

    @BeforeEach
    public void setUp(){

        counterfactory counterFactory = new javacounterfactory();
        count = null;
        count = counterFactory.getcounter();
    }

    @DisplayName("Ensure incrementing is correct")
    @Test
    void increment() {
        count.increment();
        count.increment();

        assertEquals(2, count.read(), "Failed incrementing the counter");

    }

    static class testCallable implements Callable<Void> {
        private final counter count;

        public testCallable(counter count) {
            this.count = count;
        }


        @Override
        public Void call() {
            String pid_name = Thread.currentThread().getName();
            System.out.println("my_name : "+pid_name);

            int nbOp = 10000;
            for (int i = 0; i < nbOp; i++) {
                this.count.increment();
            }
            return null;
        }
    }

    @DisplayName("Analyzing performances with multiple thread")
    @Test
    public void performancemulti() throws InterruptedException, ExecutionException {
        final int nbThreads = 1;
        final int nbTasks = 10;
        List<Callable<Void>> callables = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(nbThreads);
        for (int i = 0; i < nbTasks; i++) {
            callables.add(new testCallable(count));
        }

        double startTime = System.nanoTime();

        List<Future<Void>> futures = executor.invokeAll(callables);

        for (Future<Void> future : futures) {
            future.get();
        }
        double endTime = System.nanoTime();
        double duration = (endTime - startTime)/1000000000.0;
        System.out.println(count.read());
        System.out.println(duration+" seconds");

        executor.shutdown();
    }
}