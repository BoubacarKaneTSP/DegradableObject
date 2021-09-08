package eu.cloudbutton.dobj.types;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;

public class Factory {
    public DegradableCounter createDegradableCounter() { return new DegradableCounter(); }
    public Counter createCounter() { return new Counter(); }
    public DegradableList createDegradableList() { return new DegradableList(); }
    public ConcurrentLinkedQueue createList() { return new ConcurrentLinkedQueue(); }
    public DegradableLinkedList createDegradableLinkedList() { return new DegradableLinkedList(); }
    public LinkedList createLinkedList() { return new LinkedList(); }
    public DegradableSet createDegradableSet() { return new DegradableSet(); }
    public ConcurrentSkipListSet createSet() { return new ConcurrentSkipListSet(); }

    public CounterSnapshot createCounterSnapshot() { return new CounterSnapshot(); }
    public CounterSnapshotSRMW createCounterSnapshotSRMW(){ return new CounterSnapshotSRMW();}
    public SetSnapshot createSetSnapshot () { return new SetSnapshot(); }
    public SetSnapshotSRMW createSetSnapshotSRMW () { return new SetSnapshotSRMW(); }
    public ListSnapshot createListSnapshot () { return  new ListSnapshot(); }
    public ListSnapshotSRMW createListSnapshotSRMW() { return  new ListSnapshotSRMW(); }
    public LinkedListSnapshot createLinkedListSnapshot () { return  new LinkedListSnapshot(); }
    public LinkedListSnapshotSRMW createLinkedListSnapshotSRMW() { return  new LinkedListSnapshotSRMW(); }

    public SecondDegradableList createSecondDegradableList() {return new SecondDegradableList(); }
    public ThirdDegradableList createThirdDegradableList() {return new ThirdDegradableList(); }
    public Noop createNoop() {return new Noop(); }
}
