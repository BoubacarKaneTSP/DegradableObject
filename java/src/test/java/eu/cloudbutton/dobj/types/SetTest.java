package eu.cloudbutton.dobj.types;

import eu.cloudbutton.dobj.Factory;
import eu.cloudbutton.dobj.utils.FactoryIndice;
import eu.cloudbutton.dobj.key.Key;
import eu.cloudbutton.dobj.key.KeyGenerator;
import eu.cloudbutton.dobj.key.SimpleKeyGenerator;
import eu.cloudbutton.dobj.key.ThreadLocalKey;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.*;

public class SetTest {

    private Factory factory;
    private FactoryIndice factoryIndice;
    private KeyGenerator generator;
    private static int nbThread;

    @BeforeTest
    void setUp() {
        factory = new Factory();
        generator = new SimpleKeyGenerator(1000);
        nbThread = Runtime.getRuntime().availableProcessors();
        factoryIndice = new FactoryIndice(nbThread);
//        nbThread = 1;
    }

    @Test
    void add() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
       Class cls = Class.forName("eu.cloudbutton.dobj.asymmetric.swmr.SWMRSkipListSet");
       factory.setFactorySet(cls);
       doAdd(factory.getSet());

       cls = Class.forName("eu.cloudbutton.dobj.set.ConcurrentHashSet");
       factory.setFactorySet(cls);
       doAdd(factory.getSet());
    }

    @Test
    void remove() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class cls = Class.forName("eu.cloudbutton.dobj.asymmetric.swmr.SWMRSkipListSet");
        factory.setFactorySet(cls);
        doRemove(factory.getSet());
    }

    @Test
    void testIterator() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, ExecutionException, InterruptedException {
        Class cls = Class.forName("eu.cloudbutton.dobj.mcwmcr.SetReadIntensive");
        factory.setFactorySet(cls);
        doTestIterator(factory.getSet());
        doTestIterator(Factory.createSet("ExtendedSegmentedHashSet", factoryIndice));
    }

    private void doRemove(Set set) {
        Key k = generator.nextKey();
        set.add(k);
        set.remove(k);
        assertEquals(set.contains(k),false);
        assertEquals(set.isEmpty(),true);
    }

    private void doAdd(Set set) {
        List<Key> list = new ArrayList<>();
        int nbItem = 10;
        for(int i=0; i<10; i++) { list.add(generator.nextKey()); }

        for (int i = 0; i < nbItem; i++) set.add(list.get(i));
        assertEquals(set.containsAll(list),true);

        for (int i = 0; i < nbItem; i++) set.remove(list.get(i));
        assertEquals(set.isEmpty(),true, set.toString());
    }

    private void doTestIterator(Set set) throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(nbThread);
        List<Future<Void>> futures = new ArrayList<>();

        int nbIteration = 100;
        int nbTask = 20;

        Callable<Void> callable = () -> {

            Object obj = null;

            for (int i = 0; i < nbIteration; i++) {
                obj = new ThreadLocalKey(Thread.currentThread().getId(), i, nbIteration);
                set.add(obj);
            }
//            System.out.println(((ExtendedSegmentedHashSet<ThreadLocalKey>) set).segmentFor(obj).size());
            return null;
        };

        for (int i = 0; i < nbTask; i++) {
            futures.add(executor.submit(callable));
        }

        for (Future<Void> future : futures) {
            future.get();
        }

        int nbElement = 0;

        for (Object ignored : set){
            nbElement++;
        }

        int nbElementExpcted = nbThread > nbTask ? nbIteration * nbTask : nbIteration * nbThread;

        assertEquals(nbElement == nbElementExpcted,true, "Error with iterator");
    }

}