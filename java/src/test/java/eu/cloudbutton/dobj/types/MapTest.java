package eu.cloudbutton.dobj.types;

import eu.cloudbutton.dobj.Factory;
import eu.cloudbutton.dobj.key.Key;
import eu.cloudbutton.dobj.key.KeyGenerator;
import eu.cloudbutton.dobj.key.SimpleKeyGenerator;
import eu.cloudbutton.dobj.utils.FactoryIndice;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;

import static org.testng.Assert.*;

public class MapTest {

    private Factory factory;
    private FactoryIndice factoryIndice;
    private KeyGenerator generator;

    @BeforeMethod
    public void setUp() {
        factory = new Factory();
        factoryIndice = new FactoryIndice(Runtime.getRuntime().availableProcessors());
        generator = new SimpleKeyGenerator(1000);
    }

    @Test
    void add() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, ExecutionException, InterruptedException {
//        Class cls = Class.forName("eu.cloudbutton.dobj.asymmetric.swmr.map.SWMRHashMap");
//        factory.setFactoryMap(cls);
//        doAdd(factory.getMap());
        testSWMR(Factory.createMap("SegmentedHashMap");
//        testSWMR(factory.getMap());
//        cls = Class.forName("eu.cloudbutton.dobj.segmented.ExtendedSegmentedHashMap");
//        factory.setFactoryMap(cls);
//        doAdd(factory.getMap());
    }

    @Test
    void remove() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class cls = Class.forName("eu.cloudbutton.dobj.swsr.SWSRHashMap");
        factory.setFactoryMap(cls);
        doRemove(factory.getMap());
    }

    @Test
    void iterator() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class cls = Class.forName("eu.cloudbutton.dobj.segmented.ExtendedSegmentedConcurrentHashMap");
        factory.setFactoryMap(cls);
        doAdd(factory.getMap());
    }

    private void doAdd(Map map){
        Key k = generator.nextKey();
        map.put(k, null);
        assertEquals(map.containsKey(k), true);
    }

    private void testSWMR(Map map) throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Future<Void>> futures = new ArrayList<>();

        Queue<Key> queue = new ConcurrentLinkedQueue();


        Callable<Void> callable = () -> {

            for (int i = 0; i < 100000; i++) {
                Key k = generator.nextKey();
                map.put(k, i);
                queue.add(k);
                map.get(queue.poll());
            }

            /* if (Thread.currentThread().getName().contains("thread-1")){
                System.out.println(Thread.currentThread().getName());
                for (int i = 0; i < 100000; i++) {
                    map.put(i,generator.nextKey());
                }
            }else {
                for (int i = 0; i < 10000; i++) {
                    System.out.println(map.get(i));
                }
            }*/
            return null;
        };

        for (int i = 0; i < 1000; i++) {
            futures.add(executor.submit(callable));
        }

        for (Future<Void> future : futures) {
            future.get();
        }

    }

    private void doRemove(Map map){
        Key k = generator.nextKey();
        map.put(k, null);
        map.remove(k);
        assertEquals(map.containsKey(k), false);

    }

    private void doIterator(Map<Integer,Integer> map){
    }
}