package eu.cloudbutton.dobj.benchmark.Tester;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;

public class QueueFiller extends Filler<Queue> {

    public QueueFiller(Queue object, long nbOps) {
        super(object, nbOps);
    }

    @Override
    public void fill() throws ExecutionException, InterruptedException {

        int nbThread = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(nbThread);
        List<Future<Void>> futures = new ArrayList<>();

        int nbTask = 10;

        Callable<Void> callable = () -> {
            for (int i = 0; i < nbOps/nbTask; i++) {
                object.add(i);
            }
            return null;
        };

        for (int i = 0; i < nbTask; i++) {
            futures.add(executor.submit(callable));
        }

        for (Future<Void> future : futures) {
            future.get();
        }


    }
}
