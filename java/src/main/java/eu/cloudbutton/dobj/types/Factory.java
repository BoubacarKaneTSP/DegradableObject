package eu.cloudbutton.dobj.types;

import java.util.AbstractMap;
import java.util.AbstractQueue;
import java.util.AbstractSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;

public class Factory {

    /* Counter */

    public Counter createCounter() { return new Counter(); }
    public DegradableCounter createDegradableCounter() { return new DegradableCounter(); }
    public CounterSnapshot createCounterSnapshot() { return new CounterSnapshot(); }
    public CounterSnapshotSRMW createCounterSnapshotSRMW(){ return new CounterSnapshotSRMW(); }

    /* List */

    public DegradableList createDegradableList() { return new DegradableList(); }
    public LinkedList createLinkedList() { return new LinkedList(); }
    public DegradableLinkedList createDegradableLinkedList() { return new DegradableLinkedList(); }
    public ListSnapshot createListSnapshot () { return  new ListSnapshot(); }
    public ListSnapshotSRMW createListSnapshotSRMW() { return  new ListSnapshotSRMW(); }
    public LinkedListSnapshot createLinkedListSnapshot () { return  new LinkedListSnapshot(); }
    public LinkedListSnapshotSRMW createLinkedListSnapshotSRMW() { return  new LinkedListSnapshotSRMW(); }

    /* Set */

    public AbstractSet createSet() {return new ConcurrentSkipListSet();}
    public AbstractSet createDegradableSet() { return new DegradableSet(); }
    public AbstractSet createSetSnapshot () { return new SetSnapshot(); }
    public AbstractSet createSetSnapshotSRMW () { return new SetSnapshotSRMW(); }

    /* Queue */

    public AbstractQueue createQueue() {return new ConcurrentLinkedQueue();}
    public AbstractQueue createMapQueue() { return  new MapQueue<>(); }
    public AbstractQueue createDegradableQueue() {return new DegradableQueue(); }

    /* Map */

    public AbstractMap createDegradableMap() {return new DegradableMap(); }

    public Noop createNoop() {return new Noop(); }

}
