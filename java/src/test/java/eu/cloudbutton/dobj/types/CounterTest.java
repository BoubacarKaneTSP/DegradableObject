package eu.cloudbutton.dobj.types;

import eu.cloudbutton.dobj.incrementonly.Counter;
import eu.cloudbutton.dobj.incrementonly.FuzzyCounter;
import eu.cloudbutton.dobj.Factory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class CounterTest {

    private Factory factory;

    @BeforeTest
    public void setUp() {
        factory = new Factory();
    }
    @Test
    public void increment() throws ExecutionException, InterruptedException, InvocationTargetException, InstantiationException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException {

        /*doIncrement(factory
                .counter(new DegradableCounter())
                .build()
                .getCounter()
        );

        doIncrement(factory
                .counter(new Counter())
                .build()
                .getCounter()
        );*/

        Class cls = Class.forName("eu.cloudbutton.dobj.incrementonly.FuzzyCounter");
        factory.setFactoryCounter(cls);
//        testDifferentRead((FuzzyCounter) factory.getCounter());

        cls = Class.forName("eu.cloudbutton.dobj.incrementonly.CounterIncrementOnly");
        factory.setFactoryCounter(cls);
        doIncrement(factory.getCounter());
    }

    private static void doIncrement(Counter count) throws ExecutionException, InterruptedException {

        ExecutorService executor = Executors.newFixedThreadPool(3);
        List<Future<Void>> futures = new ArrayList<>();

        Callable<Void> callable = () -> {
            for (int i = 0; i < 100; i++) {
                count.incrementAndGet();
            }
            return null;
        };

        
        for (int i = 0; i < 10; i++) {
            futures.add(executor.submit(callable));
        }

        for (Future<Void> future : futures) {
            future.get();
        }
        assertEquals(count.read(),1000,"Failed incrementing the Counter");
    }

    private static void testDifferentRead(FuzzyCounter count) throws ExecutionException, InterruptedException {

        int nbThread = 4;
        Map<Long, ArrayList<Long>> mapRead = new ConcurrentHashMap<>();
        count.setN(nbThread);

        ExecutorService executor = Executors.newFixedThreadPool(nbThread);
        List<Future<Void>> futures = new ArrayList<>();

        Callable<Void> callable = () -> {
            long myID = Thread.currentThread().getId();
            mapRead.put(myID, new ArrayList());
            for (int i = 0; i < 100; i++) {
                mapRead.get(myID).add(count.incrementAndGet());
            }
            return null;
        };


        for (int i = 0; i < 10; i++) {
            futures.add(executor.submit(callable));
        }

        for (Future<Void> future : futures) {
            future.get();
        }

        boolean flag = true;

        for (long myID : mapRead.keySet()) {
            for (long othersID : mapRead.keySet()){

                if (myID != othersID){
                    for (long myRead: mapRead.get(myID)){
                        if (mapRead.get(othersID).contains(myRead))
                            flag = false;
                    }
                }
            }
        }
        assertTrue(flag,"Some threads have the same read");
    }
}