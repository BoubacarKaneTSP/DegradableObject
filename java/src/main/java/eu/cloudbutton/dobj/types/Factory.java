package eu.cloudbutton.dobj.types;

public class Factory {
    public AbstractCounter createDegradableCounter() { return new DegradableCounter(); }
    public AbstractCounter createCounter() { return new Counter(); }
    public AbstractList createDegradableList() { return new DegradableList(); }
    public AbstractList createList() { return new List(); }
    public AbstractList createDegradableLinkedList() { return new DegradableLinkedList(); }
    public AbstractList createLinkedList() { return new LinkedList(); }
    public AbstractSet createDegradableSet() { return new DegradableSet(); }
    public AbstractSet createSet() { return new Set(); }

    public AbstractCounter createCounterSnapshot() { return new CounterSnapshot(); }
    public AbstractCounter createCounterSnapshotSRMW(){ return new CounterSnapshotSRMW();}
    public AbstractSet createSetSnapshot () { return new SetSnapshot(); }
    public AbstractSet createSetSnapshotSRMW () { return new SetSnapshotSRMW(); }
    public AbstractList createListSnapshot () { return  new ListSnapshot(); }
    public AbstractList createListSnapshotSRMW() { return  new ListSnapshotSRMW(); }
    public AbstractList createLinkedListSnapshot () { return  new LinkedListSnapshot(); }
    public AbstractList createLinkedListSnapshotSRMW() { return  new LinkedListSnapshotSRMW(); }

    public AbstractList createSecondDegradableList() {return new SecondDegradableList(); }
    public AbstractList createThirdDegradableList() {return new ThirdDegradableList(); }
    public Noop createNoop() {return new Noop(); }
}
