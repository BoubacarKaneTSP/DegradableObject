package eu.cloudbutton.dobj.benchmark.Tester;

import eu.cloudbutton.dobj.incrementonly.AbstractCounter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class CounterFiller extends Filler<AbstractCounter> {

    public CounterFiller(AbstractCounter object, long nbOps) {
        super(object, nbOps);
    }

    @Override
    public void fill() throws ExecutionException, InterruptedException {

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Future<Void>> futures = new ArrayList<>();

        int nbTask = 10;

        Callable<Void> callable = () -> {
            for (int i = 0; i < nbOps/nbTask; i++) {
                object.incrementAndGet();
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
