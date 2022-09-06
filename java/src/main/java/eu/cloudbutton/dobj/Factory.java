package eu.cloudbutton.dobj;

import eu.cloudbutton.dobj.incrementonly.Counter;
import eu.cloudbutton.dobj.incrementonly.CounterJUC;
import eu.cloudbutton.dobj.incrementonly.FuzzyCounter;
import eu.cloudbutton.dobj.list.*;
import eu.cloudbutton.dobj.list.LinkedList;
import eu.cloudbutton.dobj.list.ListJUC;
import eu.cloudbutton.dobj.mcwmcr.MapMCWMCR;
import eu.cloudbutton.dobj.asymmetric.QueueMASP;
import eu.cloudbutton.dobj.queue.MapQueue;
import eu.cloudbutton.dobj.mcwmcr.SetMCWMCR;
import eu.cloudbutton.dobj.set.SetJUC;
import eu.cloudbutton.dobj.snapshot.*;
import eu.cloudbutton.dobj.incrementonly.CounterIncrementOnly;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

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

    public static Object createObject(String object) throws ClassNotFoundException{

        if (object.contains("Counter"))
            return createCounter(object);
        else if (object.contains("List"))
            return createList(object);
        else if (object.contains("Set"))
            return createSet(object);
        else if (object.contains("Queue"))
            return createQueue(object);
        else if (object.contains("Map"))
            return createMap(object);
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
            case "DegradableCounter":
                return new CounterIncrementOnly();
            case "CounterSnapshot":
                return new CounterSnapshot();
            case "CounterSnapshotSRMW":
                return new CounterSnapshotSRMW();
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
            case "ListSnapshot":
                return new ListSnapshot<>();
            case "ListSnapshotSRMW":
                return new ListSnapshotSRMW<>();
            case "LinkedListSnapshot":
                return new LinkedListSnapshot<>();
            case "LinkedListSnapshotSRMW":
                return new LinkedListSnapshotSRMW<>();
            default:
                throw new ClassNotFoundException();
        }
    }

    /* Set */

    public static Set createSet(String set) throws ClassNotFoundException {

        switch (set){

            case "Set":
                return new SetJUC<>();
            case "SetMCWMCR":
                return new SetMCWMCR<>();
            case "SetSnapshot":
                return new SetSnapshot<>();
            case "SetSnapshotSRMW":
                return new SetSnapshotSRMW<>();
            default:
                throw new ClassNotFoundException();
        }
    }

    /* Queue */

    public static Queue createQueue(String queue) throws ClassNotFoundException {

        switch (queue){

            case "Queue":
                return new ConcurrentLinkedQueue<>();
            case "DegradableQueue":
                return new QueueMASP<>();
            case "MapQueue":
                return new MapQueue<>();
            default:
                throw new ClassNotFoundException();

        }
    }

    /* Map */

    public static Map createMap(String map) throws ClassNotFoundException {

        switch (map){

            case "Map":
                return new ConcurrentHashMap<>();
            case "DegradableMap":
                return new MapMCWMCR<>();
            default:
                throw new ClassNotFoundException();
        }
    }

    public Noop createNoop() {return new Noop(); }

}
