package eu.cloudbutton.dobj.benchmark.Tester;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ListFiller extends Filler<AbstractList> {

    public ListFiller(AbstractList object, long nbOps) {
        super(object, nbOps);
    }

    @Override
    public void fill() throws ExecutionException, InterruptedException {

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Future<Void>> futures = new ArrayList<>();

        int nbTask = 10;

        Callable<Void> callable = () -> {

            List localList = new ArrayList();
            for (long i = 0; i < nbOps/nbTask; i++) {
                localList.add(i);
            }
            object.addAll(localList);
            System.out.println(object.size());
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