package eu.cloudbutton.dobj.types;

import java.util.AbstractMap;
import java.util.AbstractQueue;
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

    public DegradableSet createDegradableSet() { return new DegradableSet(); }
    public SetSnapshot createSetSnapshot () { return new SetSnapshot(); }
    public SetSnapshotSRMW createSetSnapshotSRMW () { return new SetSnapshotSRMW(); }

    /* Queue */

    public AbstractQueue createMapQueue() { return  new MapQueue<>(); }
    public AbstractQueue createDegradableQueue() {return new DegradableQueue(); }

    /* Map */

    public AbstractMap createDegradableMap() {return new DegradableMap(); }

    public Noop createNoop() {return new Noop(); }

}
