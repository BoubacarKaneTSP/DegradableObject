package eu.cloudbutton.dobj;

import eu.cloudbutton.dobj.types.Counter;
import eu.cloudbutton.dobj.types.CounterFactory;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class Benchmark {

    @Test
    public void counter() throws InterruptedException, ExecutionException {

        final int nbOps = 100000000;
        final int nbThreads = Runtime.getRuntime().availableProcessors()/2;
        final int nbTest = 2;

        CounterFactory counterFactory = new CounterFactory();
        // Counter count = counterFactory.createjavacounter();
        Counter count = counterFactory.createdegradablecounter();

        List<Double> result = new ArrayList<>();
        Map<Integer, List<Double>> results = new HashMap<>();

        for (int i = 1; i <= nbThreads; i++) {
            for (int a = 0; a < nbTest; a++) {
                List<Callable<Void>> callables = new ArrayList<>();
                ExecutorService executor = Executors.newFixedThreadPool(i);

                CountDownLatch latch = new CountDownLatch(i);
                for (int j = 0; j < i; j++) {
                    callables.add(new testCallable(count,latch,nbOps/i));
                }

                double startTime = System.nanoTime();

                List<Future<Void>> futures = executor.invokeAll(callables);
                for (Future<Void> future : futures) {
                    future.get();
                }
                double endTime = System.nanoTime();
                double duration = endTime - startTime;
                System.out.println(count.read()+" operations; "+i+" threads");
                System.out.println(duration+" time per op: "+ duration/(((double)nbOps)/((double)i))+"ns");
                result.add(duration);
                executor.shutdown();
                // count = counterFactory.createjavacounter();
                count = counterFactory.createdegradablecounter();
            }
            results.put(i, result);
            result = new ArrayList<>();
        }

        System.out.println(results);
    }


    private class testCallable implements Callable<Void> {

        private final Counter count;
        private final CountDownLatch latch;
        private final int nbOps;

        public testCallable(Counter count, CountDownLatch latch, int nbOps) {
            this.count = count;
            this.latch = latch;
            this.nbOps = nbOps;
        }


        @Override
        public Void call() {
/*                String pid_name = Thread.currentThread().getName();
                System.out.println("my_name : "+pid_name);*/

            latch.countDown();
            try {
                latch.await();
                for (int i = 0; i < nbOps; i++) {
                    this.count.write();
                }
            } catch (InterruptedException e) {
                //ignore
            }
            return null;
        }
    }


}
