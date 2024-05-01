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

    public Factory() {}

    public Factory(String typeMap, String typeSet, String typeQueue, String typeCounter, String typeList) throws ClassNotFoundException {
        try {
            constructorMap = toClass(typeMap).getConstructor();
            constructorSet = toClass(typeSet).getConstructor();
            constructorQueue = toClass(typeQueue).getConstructor();
            constructorCounter = toClass(typeCounter).getConstructor();
            constructorList = toClass(typeList).getConstructor();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

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

    public static Object createObject(String object) throws ClassNotFoundException{
        return newInstance(object);
    }

    /* Counter */
    public static Counter createCounter(String counter) throws ClassNotFoundException {
        return (Counter) newInstance(counter);
    }

    /* List */

    public static List createList(String list) throws ClassNotFoundException {
        return (List) newInstance(list);
    }

    /* Set */

    public static Set createSet(String set) throws ClassNotFoundException {
        return (Set) newInstance(set);
    }

    /* Queue */

    public static Queue createQueue(String queue) throws ClassNotFoundException {
       return (Queue) newInstance(queue);
    }

    /* Map */
    public static Map createMap(String map) throws ClassNotFoundException {
        return (Map) newInstance(map);
    }

    public static Object newInstance(String name) {
        try {
            return toClass(name).getConstructor().newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Class toClass(String name) throws ClassNotFoundException {
        switch (name) {
            // counter
            case "CounterJUC":
                return CounterJUC.class;
            case "CounterIncrementOnly":
                return CounterIncrementOnly.class;
            case "FuzzyCounter":
                return FuzzyCounter.class;
            case "WrappedLongAdder":
            case "LongAdder":
                return WrappedLongAdder.class;
            // list
            case "List":
                return ListJUC.class;
            case "DegradableList":
                return DegradableList.class;
            case "LinkedList":
                return LinkedList.class;
            case "ShardedLinkedList":
                return ShardedLinkedList.class;
            case "DegradableLinkedList":
                return DegradableLinkedList.class;
            // set
            case "HashSet":
                return HashSet.class;
            case "SegmentedSkipListSet":
                return SegmentedSkipListSet.class;
            case "SegmentedTreeSet":
                return SegmentedTreeSet.class;
            case "SegmentedHashSet":
                return SegmentedHashSet.class;
            case "ShardedTreeSet":
                return ShardedTreeSet.class;
            case "ShardedHashSet":
                return ShardedHashSet.class;
            case "Set":
            case "ConcurrentSkipListSet":
                return ConcurrentSkipListSet.class;
            case "ConcurrentHashSet":
                return ConcurrentHashSet.class;
            case "SetReadIntensive":
                return SetReadIntensive.class;
            case "SetAddIntensive":
                return SetAddIntensive.class;
            case "SetMWSR":
                return SetMWSR.class;
            case "ExtendedSegmentedHashSet":
                return ExtendedSegmentedHashSet.class;
            case "ExtendedShardedHashSet":
                return ExtendedShardedHashSet.class;
            // queue
            case "Queue":
            case "ConcurrentLinkedQueue":
                return ConcurrentLinkedQueue.class;
            case "Deque":
                return ConcurrentLinkedDeque.class;
            case "QueueMASP":
                return QueueMASP.class;
            case "DequeMASP":
                return DequeMASP.class;
            case "MapQueue":
                return MapQueue.class;
            case "QueueSASP":
                return QueueSASP.class;
            case "SequentialQueue":
                return java.util.LinkedList.class;
            case "WaitFreeQueue":
                return WaitFreeQueue.class;
            // map
            case "HashMap":
                return HashMap.class;
            case "SegmentedHashMap":
                return SegmentedHashMap.class;
            case "SegmentedSkipListMap":
                return SegmentedSkipListMap.class;
            case "SegmentedTreeMap":
                return SegmentedTreeMap.class;
            case "ShardedHashMap":
                return ShardedHashMap.class;
            case "Map":
            case "ConcurrentHashMap":
                return ConcurrentHashMap.class;
            case "ConcurrentSkipListMap":
                return ConcurrentSkipListMap.class;
            case "MapReadIntensive":
                return MapReadIntensive.class;
            case "MapAddIntensive":
                return MapAddIntensive.class;
            case "SWMRHashMap":
                return SWMRHashMap.class;
            case "ExtendedSegmentedHashMap":
                return ExtendedSegmentedHashMap.class;
            case "ExtendedSegmentedSkipListMap":
                return ExtendedSegmentedSkipListMap.class;
            case "ExtendedSegmentedTreeMap":
                return ExtendedSegmentedTreeMap.class;
            // other
            case "Noop":
                return Noop.class;
            case "AtomicWriteOnceReference":
                return AtomicWriteOnceReference.class;
            default:
                return Class.forName("java.util.concurrent.atomic." + name);

        }
    }


    public Noop createNoop() {return new Noop(); }

}
