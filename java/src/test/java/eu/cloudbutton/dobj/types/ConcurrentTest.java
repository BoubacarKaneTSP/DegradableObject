package eu.cloudbutton.dobj.types;

import eu.cloudbutton.dobj.Factory;
import eu.cloudbutton.dobj.utils.FactoryIndice;
import eu.cloudbutton.dobj.asymmetric.swmr.map.SWMRHashMap;
import eu.cloudbutton.dobj.key.ThreadLocalKey;
import eu.cloudbutton.dobj.segmented.ExtendedSegmentedHashMap;
import eu.cloudbutton.dobj.segmented.ExtendedSegmentedHashSet;
import eu.cloudbutton.dobj.swsr.SWSRHashSet;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

public class ConcurrentTest {

    private Factory factory;
    private FactoryIndice factoryIndice;
    private static Integer nbThread;
    private static ExecutorService executor;

    @BeforeTest
    void setUp() {
        factory = new Factory();
        nbThread = Runtime.getRuntime().availableProcessors();
//        nbThread = 1;
        factoryIndice = new FactoryIndice(nbThread);
        executor = Executors.newFixedThreadPool(nbThread);
    }


    @Test
    void add() throws ExecutionException, InterruptedException, ClassNotFoundException {
        addExtendedSegmentedHashMap((ExtendedSegmentedHashMap<ThreadLocalKey, String>) Factory.createMap("ExtendedSegmentedHashMap", factoryIndice));
        addExtendedSegmentedHashSet((ExtendedSegmentedHashSet<ThreadLocalKey>) Factory.createSet("ExtendedSegmentedHashSet" , factoryIndice));
//        concurrentSWMRMapTest(Factory.createMap("ExtendedSegmentedHashMap", factoryIndice));
    }

    private static void addExtendedSegmentedHashMap(ExtendedSegmentedHashMap<ThreadLocalKey, String> obj) throws ExecutionException, InterruptedException {
        List<Future<Void>> futures = new ArrayList<>();

        int nbIteration = 100;
        Callable<Void> callable = () -> {
            for (int i = 0; i < nbIteration; i++) {
                ThreadLocalKey key = new ThreadLocalKey(Thread.currentThread().getId(), i, nbIteration);

                obj.put(key, Thread.currentThread().getName());
                SWMRHashMap<ThreadLocalKey, String> map = obj.segmentFor(key);

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

    private static void addExtendedSegmentedHashSet(ExtendedSegmentedHashSet<ThreadLocalKey> obj) throws ExecutionException, InterruptedException {
        List<Future<Void>> futures = new ArrayList<>();

        int nbIteration = 100;
        Callable<Void> callable = () -> {
            for (int i = 0; i < nbIteration; i++) {
                ThreadLocalKey key = new ThreadLocalKey(Thread.currentThread().getId(), i, nbIteration);

                obj.add(key);
                SWSRHashSet<ThreadLocalKey> set = obj.segmentFor(key);

                for (ThreadLocalKey k : set){
                    assert k.tid == Thread.currentThread().getId() : "Reading the wrong segment";
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
        List<ThreadLocalKey> list = new CopyOnWriteArrayList<>();
        AtomicReference<ThreadLocalRandom> random = new AtomicReference<>();
        int nbIteration = 1000;
        Callable<Void> callable = () -> {
            random.set(ThreadLocalRandom.current());
            String name = Thread.currentThread().getName();
            ThreadLocalKey key = new ThreadLocalKey(Thread.currentThread().getId(), 0, nbIteration);
            map.put(key, 0);
            list.add(key);
            assert map.get(key) == 0 : "error with put method";

            for (int i = 0; i < nbIteration; i++) {
                if (name.contains("thread-1")){
                    key = new ThreadLocalKey(Thread.currentThread().getId(), i, nbIteration);
                    map.put(key, i);
                }else{
                    int val = random.get().nextInt(list.size());
                    ThreadLocalKey key1 = list.get(val);
                    assert map.get(key1) != null : "get shouldn't return null : " + Thread.currentThread().getName();
                }
            }
            return null;
        };

        for (int i = 0; i < nbThread; i++) {
            futures.add(executor.submit(callable));
        }

        for (Future<Void> future :futures){
            future.get();
        }
    }
}
