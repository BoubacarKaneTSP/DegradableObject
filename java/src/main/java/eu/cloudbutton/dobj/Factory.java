package eu.cloudbutton.dobj;

import eu.cloudbutton.dobj.asymmetric.QueueMASP;
import eu.cloudbutton.dobj.asymmetric.QueueSASP;
import eu.cloudbutton.dobj.asymmetric.SetMWSR;
import eu.cloudbutton.dobj.incrementonly.Counter;
import eu.cloudbutton.dobj.incrementonly.CounterIncrementOnly;
import eu.cloudbutton.dobj.incrementonly.CounterJUC;
import eu.cloudbutton.dobj.incrementonly.FuzzyCounter;
import eu.cloudbutton.dobj.list.DegradableLinkedList;
import eu.cloudbutton.dobj.list.DegradableList;
import eu.cloudbutton.dobj.list.LinkedList;
import eu.cloudbutton.dobj.list.ListJUC;
import eu.cloudbutton.dobj.mcwmcr.MapAddIntensive;
import eu.cloudbutton.dobj.mcwmcr.MapReadIntensive;
import eu.cloudbutton.dobj.mcwmcr.SetAddIntensive;
import eu.cloudbutton.dobj.mcwmcr.SetReadIntensive;
import eu.cloudbutton.dobj.queue.MapQueue;
import eu.cloudbutton.dobj.segmented.SegmentedHashMap;
import eu.cloudbutton.dobj.segmented.SegmentedTreeSet;
import eu.cloudbutton.dobj.sharded.ShardedHashMap;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;

public class Factory {

    private Constructor<? extends AbstractMap> constructorMap;
    private Constructor<? extends AbstractList> constructorList;
    private Constructor<? extends AbstractSet> constructorSet;
    private Constructor<? extends AbstractQueue> constructorQueue;
    private Constructor<? extends Counter> constructorCounter;

    public void setFactoryMap(Class<? extends AbstractMap> mapClass) throws NoSuchMethodException {
        constructorMap = mapClass.getConstructor();
    }
    public void setFactoryCounter(Class<? extends Counter> counterClass) throws NoSuchMethodException {
        constructorCounter = counterClass.getConstructor();
    }
    public void setFactorySet(Class<? extends AbstractSet> setClass) throws NoSuchMethodException{
        constructorSet = setClass.getConstructor();
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

        if (object.contains("Counter"))
            return createCounter(object);
        else if (object.contains("List"))
            return createList(object);
        else if (object.contains("Set"))
            return createSet(object, parallelism);
        else if (object.contains("Queue"))
            return createQueue(object);
        else if (object.contains("Map"))
            return createMap(object, parallelism);
        else if (object.contains("Noop"))
            return new Noop();
        else
            throw new ClassNotFoundException("The object : "+ object +" may not exists");
    }
    /* Counter */

    public static Counter createCounter(String counter) throws ClassNotFoundException {

        switch (counter){

            case "Counter":
                return new CounterJUC();
            case "CounterIncrementOnly":
                return new CounterIncrementOnly();
            case "FuzzyCounter":
                return new FuzzyCounter();
            default:
                throw new ClassNotFoundException();
        }
    }

    /* List */

    public static List createList(String list) throws ClassNotFoundException {

        switch (list){

            case "List":
                return new ListJUC();
            case "DegradableList":
                return new DegradableList();
            case "LinkedList":
                return new LinkedList<>();
            case "DegradableLinkedList":
                return new DegradableLinkedList<>();
            default:
                throw new ClassNotFoundException();
        }
    }

    /* Set */

    public static Set createSet(String set, int parallelism) throws ClassNotFoundException {

        switch (set){
            case "SegmentedTreeSet":
                System.out.println("new SegmentedHashMap("+parallelism+")");
                return new SegmentedTreeSet(parallelism);
            case "Set":
                return new ConcurrentSkipListSet<>();
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

    /* Queue */

    public static Queue createQueue(String queue) throws ClassNotFoundException {

        switch (queue){

            case "Queue":
                return new ConcurrentLinkedQueue();
            case "QueueMASP":
                return new QueueMASP<>();
            case "MapQueue":
                return new MapQueue<>();
            case "QueueSASP":
                return new QueueSASP<>();
            case "SequentialQueue":
                return new java.util.LinkedList<>();
            default:
                throw new ClassNotFoundException();

        }
    }

    /* Map */

    public static Map createMap(String map, int parallelism) throws ClassNotFoundException {

        switch (map){

            case "SegmentedHashMap":
                System.out.println("new SegmentedHashMap("+parallelism+")");
                return new SegmentedHashMap(parallelism);
            case "ShardedHashMap":
                System.out.println("new ShardedHashMap("+parallelism+")");
                return new ShardedHashMap(parallelism);
            case "Map":
                return new ConcurrentHashMap<>();
            case "MapReadIntensive":
                return new MapReadIntensive<>();
            case "MapAddIntensive":
                return new MapAddIntensive<>();
            default:
                throw new ClassNotFoundException();
        }
    }

    public Noop createNoop() {return new Noop(); }

}
