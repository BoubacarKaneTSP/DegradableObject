import factories.counter;
import factories.counterfactory;
import factories.degradablecounterfactory;
//import factories.javacounterfactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class Benchmark {

    public static void main(String[] args) throws InterruptedException, ExecutionException {

        class testCallable implements Callable<Void> {
            private final counter count;

            public testCallable(counter count) {
                this.count = count;
            }


            @Override
            public Void call() {
/*                String pid_name = Thread.currentThread().getName();
                System.out.println("my_name : "+pid_name);*/

                int nbOp = 10000000;
                for (int i = 0; i < nbOp; i++) {
                    this.count.write();
                }
                return null;
            }
        }

        final int nbTasks = 10;
        final int nbThreads = 5;
        final int nbTest = 2;

        counterfactory counterFactory = new degradablecounterfactory();
        counter count = counterFactory.getcounter();

        List<Double> result = new ArrayList<>();
        Map<Integer, List<Double>> results = new HashMap<>();

        for (int i = 1; i <= nbThreads; i++) {
            for (int a = 0; a < nbTest; a++) {
                List<Callable<Void>> callables = new ArrayList<>();
                ExecutorService executor = Executors.newFixedThreadPool(i);

                for (int j = 0; j < nbTasks; j++) {
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
                System.out.println(duration+" seconds, "+ "nbThreads = "+i+", test num : "+a);
                result.add(duration);
                executor.shutdown();
                count = counterFactory.getcounter();
            }
            results.put(i, result);
            result = new ArrayList<>();
        }

        System.out.println(results);
    }
}
