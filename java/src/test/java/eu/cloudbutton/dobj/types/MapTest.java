package eu.cloudbutton.dobj.types;

import eu.cloudbutton.dobj.Factory;
import eu.cloudbutton.dobj.key.Key;
import eu.cloudbutton.dobj.key.KeyGenerator;
import eu.cloudbutton.dobj.key.SimpleKeyGenerator;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import static org.testng.Assert.*;

public class MapTest {

    private Factory factory;
    private KeyGenerator generator;

    @BeforeMethod
    public void setUp() {
        factory = new Factory();
        generator = new SimpleKeyGenerator(1000);
    }

    @Test
    void add() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class cls = Class.forName("eu.cloudbutton.dobj.asymmetric.swmr.map.SWMRHashMap");
        factory.setFactoryMap(cls);
        doAdd(factory.getMap());

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

    private void doRemove(Map map){
        Key k = generator.nextKey();
        map.put(k, null);
        map.remove(k);
        assertEquals(map.containsKey(k), false);

    }

    private void doIterator(Map<Integer,Integer> map){
    }
}