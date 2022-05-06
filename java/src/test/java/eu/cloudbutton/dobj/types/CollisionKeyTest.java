package eu.cloudbutton.dobj.types;

import eu.cloudbutton.dobj.map.CollisionKey;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class CollisionKeyTest {

    @Test
    public void PerformanceCollisionTest() throws ExecutionException, InterruptedException {
/*
        ConcurrentHashMap<CollisionKey, String> collisionMap = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() / 2);
        List<Future<Void>> futures = new ArrayList<>();

        Callable<Void> callable = () -> {
            String currentThreadName = Thread.currentThread().getName();

            for (int i = 0; i < 5_000; i++) {
                collisionMap.put(new CollisionKey( currentThreadName + "_" + i),"value");
//                map.put(currentThreadName +"_"+ i , "value");
            }
            return null;
        };

        long start, end;

        start = System.nanoTime();

        for (int i = 0; i < 10; i++) {
            futures.add(executor.submit(callable));
        }

        for (Future<Void> future : futures) {
            future.get();
        }

        end = System.nanoTime();
        double timeElapsed =  (end - start) / 1000000000.0;*/
/*
        System.out.println("Number of processes => " + Runtime.getRuntime().availableProcessors()/2);
        System.out.println("Time elapsed filling the hashmap => " + timeElapsed + " seconds.");
        System.out.println("Current map size => " + map.size());
        System.out.println("Current collisionMap size => " + collisionMap.size());*/

    }

}