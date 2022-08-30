package eu.cloudbutton.dobj.types;

import eu.cloudbutton.dobj.Benchmark.Benchmark;
import eu.cloudbutton.dobj.map.AbstractCollisionKey;
import eu.cloudbutton.dobj.map.CollisionKeyFactory;
import eu.cloudbutton.dobj.map.DegradableMap;
import eu.cloudbutton.dobj.map.PowerLawCollisionKey;
import org.testng.annotations.Test;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class PowerLawCollisionKeyTest {

    @Test
    public void PerformanceCollisionTest() throws ExecutionException, InterruptedException {

        AbstractMap<AbstractCollisionKey, String> collisionMap = new DegradableMap<>();
        AbstractMap<String, String> map = new DegradableMap<>();
        CollisionKeyFactory factory = new CollisionKeyFactory();

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Future<Void>> futures = new ArrayList<>();

        Callable<Void> callable = () -> {
            factory.setFactoryCollisionKey(PowerLawCollisionKey.class);

            for (int i = 0; i < 10_000; i++) {
//                collisionMap.put(factory.getCollisionKey(), "value");
                map.put(Thread.currentThread().getName(), String.valueOf(i));
                map.get(Thread.currentThread().getName());
            }
            return null;
        };

        long start, end;

        start = System.nanoTime();

        for (int i = 0; i < 100; i++) {
            futures.add(executor.submit(callable));
        }

        for (Future<Void> future : futures) {
            future.get();
        }

        System.out.println(((DegradableMap)map).getListMap());
        end = System.nanoTime();
        double timeElapsed =  (end - start) / 1000000000.0;

        System.out.println("Number of processes => " + Runtime.getRuntime().availableProcessors()/2);
        System.out.println("Time elapsed filling the hashmap => " + timeElapsed + " seconds.");
        System.out.println("Current map size => " + map.size());
        System.out.println("Current collisionMap size => " + collisionMap.size());

    }

}