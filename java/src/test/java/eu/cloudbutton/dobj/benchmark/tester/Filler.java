package eu.cloudbutton.dobj.benchmark.tester;

import eu.cloudbutton.dobj.key.Key;
import eu.cloudbutton.dobj.key.KeyGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public abstract class Filler<T> {

    protected final T object;
    protected final long nbOp;
    protected final KeyGenerator generator; 

    public Filler(T object, KeyGenerator generator, long nbOp) {
        this.object = object;
        this.nbOp = nbOp;
        this.generator = generator;
    }

    public final void fill() {
        int nworkers = 1; // Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(nworkers);
        List<Future<Void>> futures = new ArrayList<>();

        int nbTask = Runtime.getRuntime().availableProcessors();
        for (int i = 0; i < nbTask; i++) {
            long range = nbOp /nbTask;
            long min = i * range;
            long max = i != nbTask-1 ? range*(i+1) : nbOp;
            futures.add(executor.submit(new FillerCallable(min, max)));
        }

        for (Future<Void> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

    }

    public abstract void doFill(Key key);

    private class FillerCallable<T> implements Callable<Void> {

        private final long min, max;

        FillerCallable(long min, long max) {
            this.min = min;
            this.max = max;
        }

        @Override
        public Void call() throws Exception {
            for (long i = min; i < max; i++) {
                Key key = generator.nextKey();
                doFill(key);
            }
            return null;
        }
    }
}
