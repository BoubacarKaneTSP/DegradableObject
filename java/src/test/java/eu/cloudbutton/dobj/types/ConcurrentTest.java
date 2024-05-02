package eu.cloudbutton.dobj.types;

import eu.cloudbutton.dobj.Factory;
import eu.cloudbutton.dobj.key.ThreadLocalKey;
import eu.cloudbutton.dobj.segmented.ExtendedSegmentedHashMap;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

public class ConcurrentTest {

    private Factory factory;
    private static Integer nbThread;
    private static ExecutorService executor;

    @BeforeTest
    void setUp() {
        factory = new Factory();
        nbThread = Runtime.getRuntime().availableProcessors();
//        nbThread = 1;
        executor = Executors.newFixedThreadPool(nbThread);
    }


    @Test
    void add() throws ExecutionException, InterruptedException, ClassNotFoundException {
        addExtendedSegmentedHashMap((ExtendedSegmentedHashMap<ThreadLocalKey, String>) Factory.newObject("ExtendedSegmentedHashMap"));
        concurrentSWMRMapTest((Map<ThreadLocalKey, Integer>) Factory.newObject("SegmentedHashMap"));
    }

    private static void addExtendedSegmentedHashMap(ExtendedSegmentedHashMap<ThreadLocalKey, String> obj) throws ExecutionException, InterruptedException {
        List<Future<Void>> futures = new ArrayList<>();

        int nbIteration = 10000;
        Callable<Void> callable = () -> {
            for (int i = 0; i < nbIteration; i++) {
                ThreadLocalKey key = new ThreadLocalKey(Thread.currentThread().getId(), i);

                obj.put(key, Thread.currentThread().getName());
                Map<ThreadLocalKey, String> map = obj.segmentFor(key);

                for (String s : map.values() ){
                    assert s.equals(Thread.currentThread().getName()) : "Thread : "+ Thread.currentThread().getName() +" => values : " + map.values();
                }

            }
            return null;
        };

        for (int i = 0; i < 20; i++) {
            futures.add(executor.submit(callable));
        }

        for (Future<Void> future :futures){
            future.get();
        }
    }

    private static void concurrentSWMRMapTest(Map<ThreadLocalKey, Integer> map) throws ExecutionException, InterruptedException {
        List<Future<Void>> futures = new ArrayList<>();
        Queue<ThreadLocalKey> list = new ConcurrentLinkedQueue<>();
        AtomicReference<ThreadLocalRandom> random = new AtomicReference<>();
        int nbIteration = 10000;
        Callable<Void> callable = () -> {
            random.set(ThreadLocalRandom.current());
            ThreadLocalKey key = new ThreadLocalKey(Thread.currentThread().getId(), 0);
            map.put(key, 0);
            list.add(key);
            assert map.get(key) == 0 : "error with put method";

            for (int i = 0; i < nbIteration; i++) {
                key = new ThreadLocalKey(Thread.currentThread().getId(), i);
                map.put(key, i);
                list.add(key);

                ThreadLocalKey key1 = list.poll();
                if (key1 != null)
                    assert map.get(key1) != null : "get shouldn't return null : " + Thread.currentThread().getName();

            }
            return null;
        };

        for (int i = 0; i < 2*nbThread; i++) {
            futures.add(executor.submit(callable));
        }

        for (Future<Void> future :futures){
            future.get();
        }
    }
}
