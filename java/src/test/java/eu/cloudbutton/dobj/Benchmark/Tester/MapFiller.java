package eu.cloudbutton.dobj.Benchmark.Tester;

import eu.cloudbutton.dobj.map.CollisionKeyFactory;
import eu.cloudbutton.dobj.map.PowerLawCollisionKey;

import java.lang.reflect.InvocationTargetException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class MapFiller extends Filler<AbstractMap> {

    private boolean useCollisionKey;

    public MapFiller(AbstractMap map, long nbOps, boolean useCollisionKey) {
        super(map, nbOps);
        this.useCollisionKey = useCollisionKey;
    }

    @Override
    public void fill() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, ExecutionException, InterruptedException {

        CollisionKeyFactory factory = new CollisionKeyFactory();

        factory.setFactoryCollisionKey(PowerLawCollisionKey.class);

        int nbTask = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(1);
        List<Future<Void>> futures = new ArrayList<>();



        Callable<Void> callable = () -> {

            System.out.println("Je suis le thread : " + Thread.currentThread().getName() + " et je commence à ajouter mes " + nbOps/nbTask +" objets");
            for (int i = 0; i < nbOps/nbTask; i++) {
                object.put(factory.getCollisionKey(), i);
            }
            System.out.println("Je suis le thread : " + Thread.currentThread().getName() + " et j'ai finis d'ajouter mes objets");
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
