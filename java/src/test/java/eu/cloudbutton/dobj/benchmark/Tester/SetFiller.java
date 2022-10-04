package eu.cloudbutton.dobj.benchmark.Tester;

import eu.cloudbutton.dobj.map.CollisionKeyFactory;
import eu.cloudbutton.dobj.map.PowerLawCollisionKey;

import java.lang.reflect.InvocationTargetException;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

public class SetFiller extends Filler<Set>{

    private boolean useCollisionKey;

    public SetFiller(Set set, long nbOps, boolean useCollisionKey) {
        super(set, nbOps);
        this.useCollisionKey = useCollisionKey;
    }

    @Override
    public void fill() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, ExecutionException, InterruptedException {

        CollisionKeyFactory factory = null;

        if (useCollisionKey){
            factory = new CollisionKeyFactory();
            factory.setFactoryCollisionKey(PowerLawCollisionKey.class);
        }

	ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Future<Void>> futures = new ArrayList<>();

        int nbTask = 10;

        CollisionKeyFactory finalFactory = factory;
        Callable<Void> callable = () -> {

            for (long i = 0; i < nbOps/nbTask; i++) {
                if(useCollisionKey)
                    object.add(finalFactory.getCollisionKey());
                else
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
