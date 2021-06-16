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
    public AbstractCounter createCounterSnapshotV2(){ return new CounterSnapshotV2();}
    public AbstractSet createSetSnapshot () { return new SetSnapshot(); }
    public AbstractSet createSetSnapshotV2 () { return new SetSnapshotV2(); }
    public AbstractList createListSnapshot () { return  new ListSnapshot(); }
    public AbstractList createListSnapshotV2 () { return  new ListSnapshotV2(); }
    public AbstractList createLinkedListSnapshot () { return  new LinkedListSnapshot(); }
    public AbstractList createLinkedListSnapshotV2 () { return  new LinkedListSnapshotV2(); }

    public AbstractList createSecondDegradableList() {return new SecondDegradableList(); }
    public AbstractList createThirdDegradableList() {return new ThirdDegradableList(); }
}
