package eu.cloudbutton.dobj.benchmark.tester;

import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class QueueFiller extends Filler<AbstractQueue> {

    public QueueFiller(AbstractQueue object, long nbOps) {
        super(object, nbOps);
    }

    @Override
    public void fill() throws ExecutionException, InterruptedException {

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
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
