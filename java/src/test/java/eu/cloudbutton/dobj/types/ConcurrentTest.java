package eu.cloudbutton.dobj.types;

import eu.cloudbutton.dobj.Factory;
import eu.cloudbutton.dobj.asymmetric.swmr.SWMRHashMap;
import eu.cloudbutton.dobj.key.ThreadLocalKey;
import eu.cloudbutton.dobj.segmented.ExtendedSegmentedHashMap;
import eu.cloudbutton.dobj.segmented.ExtendedSegmentedHashSet;
import eu.cloudbutton.dobj.swsr.SWSRHashSet;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ConcurrentTest {

    private Factory factory;
    private static Integer nbThread;

    @BeforeTest
    void setUp() {
        factory = new Factory();
//        nbThread = 1;
        nbThread = Runtime.getRuntime().availableProcessors();
    }


    @Test
    void add() throws ExecutionException, InterruptedException, InvocationTargetException, InstantiationException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException {
        addExtendedSegmentedHashMap((ExtendedSegmentedHashMap<ThreadLocalKey, String>) Factory.createMap("ExtendedSegmentedHashMap", nbThread));
        addExtendedSegmentedHashSet((ExtendedSegmentedHashSet<ThreadLocalKey>) Factory.createSet("ExtendedSegmentedHashSet" , nbThread));
    }

    private static void addExtendedSegmentedHashMap(ExtendedSegmentedHashMap<ThreadLocalKey, String> obj) throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(nbThread);
        List<Future<Void>> futures = new ArrayList<>();

        int nbIteration = 100;
        Callable<Void> callable = () -> {
            for (int i = 0; i < nbIteration; i++) {
                ThreadLocalKey key = new ThreadLocalKey(Thread.currentThread().getId(), i, nbIteration);

                obj.put(key, Thread.currentThread().getName());
                SWMRHashMap<ThreadLocalKey, String> map = obj.segmentFor(key);

                for (String s : map.values() ){
                    assert s.equals(Thread.currentThread().getName()) : "Reading the wrong segment";
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
        ExecutorService executor = Executors.newFixedThreadPool(nbThread);
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
}
