package eu.cloudbutton.dobj.types;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.AbstractQueue;
import java.util.AbstractSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;

@Builder
@Getter
public class Factory {

    private AbstractMap map;
    private AbstractList list;
    private AbstractSet set;
    private AbstractQueue queue;
    private AbstractCounter counter;
    private Noop noop;

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
        else
            throw new ClassNotFoundException("The object may not exists");
    }
    /* Counter */

    public static AbstractCounter createCounter(String counter) throws ClassNotFoundException {

        switch (counter){

            case "Counter":
                return new Counter();
            case "DegradableCounter":
                return new DegradableCounter();
            case "CounterSnapshot":
                return new CounterSnapshot();
            case "CounterSnapshotSRMW":
                return new CounterSnapshotSRMW();
            default:
                throw new ClassNotFoundException();
        }
    }

    /* List */

    public static AbstractList createList(String list) throws ClassNotFoundException {

        switch (list){

            case "DegradableList":
                return new DegradableList<>();
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

    public static AbstractSet createSet(String set) throws ClassNotFoundException {

        switch (set){

            case "Set":
                return new Set<>();
            case "DegradableSet":
                return new DegradableSet<>();
            case "SetSnapshot":
                return new SetSnapshot<>();
            case "SetSnapshotSRMW":
                return new SetSnapshotSRMW<>();
            default:
                throw new ClassNotFoundException();
        }
    }

    /* Queue */

    public static AbstractQueue createQueue(String queue) throws ClassNotFoundException {

        switch (queue){

            case "Queue":
                return new ConcurrentLinkedQueue<>();
            case "DegradableQueue":
                return new DegradableQueue<>();
            case "MapQueue":
                return new MapQueue<>();
            default:
                throw new ClassNotFoundException();

        }
    }

    /* Map */

    public static AbstractMap createMap(String map) throws ClassNotFoundException {

        switch (map){

            case "Map":
                return new ConcurrentHashMap<>();
            case "DegradableMap":
                return new DegradableMap<>();
            default:
                throw new ClassNotFoundException();
        }
    }

    public Noop createNoop() {return new Noop(); }

}
