package eu.cloudbutton.dobj;

import eu.cloudbutton.dobj.asymmetric.DequeMASP;
import eu.cloudbutton.dobj.asymmetric.QueueMASP;
import eu.cloudbutton.dobj.asymmetric.QueueSASP;
import eu.cloudbutton.dobj.set.SetMWSR;
import eu.cloudbutton.dobj.asymmetric.swmr.map.SWMRHashMap;
import eu.cloudbutton.dobj.incrementonly.*;
import eu.cloudbutton.dobj.list.DegradableLinkedList;
import eu.cloudbutton.dobj.list.DegradableList;
import eu.cloudbutton.dobj.list.LinkedList;
import eu.cloudbutton.dobj.list.ListJUC;
import eu.cloudbutton.dobj.mcwmcr.MapAddIntensive;
import eu.cloudbutton.dobj.mcwmcr.MapReadIntensive;
import eu.cloudbutton.dobj.mcwmcr.SetAddIntensive;
import eu.cloudbutton.dobj.mcwmcr.SetReadIntensive;
import eu.cloudbutton.dobj.queue.MapQueue;
import eu.cloudbutton.dobj.queue.WaitFreeQueue;
import eu.cloudbutton.dobj.register.AtomicWriteOnceReference;
import eu.cloudbutton.dobj.segmented.*;
import eu.cloudbutton.dobj.set.ConcurrentHashSet;
import eu.cloudbutton.dobj.sharded.*;
import eu.cloudbutton.dobj.utils.FactoryIndice;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.*;

public class Factory {

    private Constructor<? extends AbstractMap> constructorMap;
    private Constructor<? extends AbstractList> constructorList;
    private Constructor<? extends AbstractSet> constructorSet;
    private Constructor<? extends AbstractQueue> constructorQueue;
    private Constructor<? extends Counter> constructorCounter;

    public void setFactoryMap(Class<? extends AbstractMap> mapClass) throws NoSuchMethodException {
        constructorMap = mapClass.getConstructor();
    }
    public void setFactoryMap(Class<? extends AbstractMap> mapClass, int parallelism) throws NoSuchMethodException {
        constructorMap = mapClass.getDeclaredConstructor();
    }
    public void setFactoryCounter(Class<? extends Counter> counterClass) throws NoSuchMethodException {
        constructorCounter = counterClass.getConstructor();
    }
    public void setFactorySet(Class<? extends AbstractSet> setClass) throws NoSuchMethodException{
        constructorSet = setClass.getDeclaredConstructor();
    }
    public void setFactoryList(Class<? extends AbstractList> listClass) throws NoSuchMethodException{
        constructorList = listClass.getConstructor();
    }
    public void setFactoryQueue(Class<? extends AbstractQueue> queueClass) throws NoSuchMethodException{
        constructorQueue = queueClass.getConstructor();
    }

    public Map getMap() throws InvocationTargetException, InstantiationException, IllegalAccessException {
        return constructorMap.newInstance();
    }
    public Counter getCounter() throws InvocationTargetException, InstantiationException, IllegalAccessException {
        return constructorCounter.newInstance();
    }
    public Set getSet() throws InvocationTargetException, InstantiationException, IllegalAccessException {
        return constructorSet.newInstance();
    }
    public Queue getQueue() throws InvocationTargetException, InstantiationException, IllegalAccessException {
        return constructorQueue.newInstance();
    }
    public List getList() throws InvocationTargetException, InstantiationException, IllegalAccessException {
        return constructorList.newInstance();
    }

    public static Object createObject(String object, int parallelism) throws ClassNotFoundException{

        if (object.contains("Counter") || object.contains("LongAdder"))
            return createCounter(object);
        else if (object.contains("Set"))
            return createSet(object, parallelism);
        else if (object.contains("Map"))
            return createMap(object, parallelism);
        else if (object.contains("List"))
            return createList(object, parallelism);
        else if (object.contains("Queue"))
            return createQueue(object);
        else if (object.contains("Noop"))
            return new Noop();
        else if (object.contains("Reference")) {
            if (object.equals("AtomicWriteOnceReference"))
                return new AtomicWriteOnceReference<>();
            else {
                try{
                    return Class.forName("java.util.concurrent.atomic." + object).getConstructor().newInstance();
                } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
                    System.err.println("The class \"" + object + "\" may not exists");
                    e.printStackTrace();
                }
            }
        }
        else
            throw new ClassNotFoundException("The object : "+ object +" may not exists");
        return null;
    }

    public static Object createObject(String object, FactoryIndice factoryIndice) throws ClassNotFoundException{

        if (object.contains("Set"))
            return createSet(object, factoryIndice);
        else if (object.contains("Map"))
            return createMap(object, factoryIndice);
        else
            throw new ClassNotFoundException("The object : "+ object +" may not exists");
    }
    /* Counter */

    public static Counter createCounter(String counter) throws ClassNotFoundException {

        switch (counter){

            case "CounterJUC":
                return new CounterJUC();
            case "CounterIncrementOnly":
                return new CounterIncrementOnly();
            case "FuzzyCounter":
                return new FuzzyCounter();
            case "WrappedLongAdder":
                return new WrappedLongAdder();
            default:
                throw new ClassNotFoundException();
        }
    }

    /* List */

    public static List createList(String list, int parallelism) throws ClassNotFoundException {

        switch (list){

            case "List":
                return new ListJUC<>();
            case "DegradableList":
                return new DegradableList<>();
            case "LinkedList":
                return new LinkedList<>();
            case "ShardedLinkedList":
                return new ShardedLinkedList<>(parallelism);
            case "DegradableLinkedList":
                return new DegradableLinkedList<>();
            default:
                throw new ClassNotFoundException();
        }
    }

    /* Set */

    public static Set createSet(String set, int parallelism) throws ClassNotFoundException {

        switch (set){
            case "HashSet":
                return new HashSet<>();
            case "SegmentedSkipListSet":
                return new SegmentedSkipListSet<>(parallelism);
            case "SegmentedTreeSet":
                return new SegmentedTreeSet<>(parallelism);
            case "SegmentedHashSet":
                return new SegmentedHashSet<>(parallelism);
            case "ShardedTreeSet":
                return new ShardedTreeSet<>(parallelism);
            case "ShardedHashSet":
                return new ShardedHashSet<>(parallelism);
            case "Set":
            case "ConcurrentSkipListSet":
                return new ConcurrentSkipListSet<>();
            case "ConcurrentHashSet":
                return new ConcurrentHashSet<>();
            case "SetReadIntensive":
                return new SetReadIntensive<>();
            case "SetAddIntensive":
                return new SetAddIntensive<>();
            case "SetMWSR":
                return new SetMWSR<>();
            default:
                throw new ClassNotFoundException();
        }
    }

    public static Set createSet(String set, FactoryIndice factoryIndice) throws ClassNotFoundException {

        switch (set){
            case "ExtendedSegmentedHashSet":
                return new ExtendedSegmentedHashSet<>(factoryIndice);
            case "ExtendedShardedHashSet":
                return new ExtendedShardedHashSet<>(factoryIndice);
            default:
                throw new ClassNotFoundException();
        }
    }

    /* Queue */

    public static Queue createQueue(String queue) throws ClassNotFoundException {

        switch (queue){

            case "Queue":
            case "ConcurrentLinkedQueue":
                return new ConcurrentLinkedQueue<>();
            case "Deque":
                return new ConcurrentLinkedDeque();
            case "QueueMASP":
                return new QueueMASP<>();
            case "DequeMASP":
                return new DequeMASP<>();
            case "MapQueue":
                return new MapQueue<>();
            case "QueueSASP":
                return new QueueSASP<>();
            case "SequentialQueue":
                return new java.util.LinkedList<>();
            case "WaitFreeQueue":
                return new WaitFreeQueue<>();
            default:
                throw new ClassNotFoundException();

        }
    }

    /* Map */

    public static Map createMap(String map, int parallelism) throws ClassNotFoundException {

        switch (map){

            case "SegmentedHashMap":
                return new SegmentedHashMap<>(parallelism);
            case "SegmentedSkipListMap":
                return new SegmentedSkipListMap<>(parallelism);
            case "SegmentedTreeMap":
                return new SegmentedTreeMap<>(parallelism);
            case "ShardedHashMap":
                return new ShardedHashMap<>(parallelism);
            case "Map":
            case "ConcurrentHashMap":
                return new ConcurrentHashMap<>();
            case "ConcurrentSkipListMap":
                return new ConcurrentSkipListMap<>();
            case "MapReadIntensive":
                return new MapReadIntensive<>();
            case "MapAddIntensive":
                return new MapAddIntensive<>();
            case "SWMRHashMap":
                return new SWMRHashMap();
            default:
                throw new ClassNotFoundException();
        }
    }

    public static Map createMap(String map, FactoryIndice factoryIndice) throws ClassNotFoundException {

        switch (map){
            case "ExtendedSegmentedHashMap":
                return new ExtendedSegmentedHashMap<>(factoryIndice);
            case "ExtendedSegmentedConcurrentHashMap":
                return new ExtendedSegmentedConcurrentHashMap<>(factoryIndice);
            case "ExtendedSegmentedSkipListMap":
                return new ExtendedSegmentedSkipListMap<>(factoryIndice);
            default:
                throw new ClassNotFoundException();
        }
    }

    public Noop createNoop() {return new Noop(); }

}
