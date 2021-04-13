package eu.cloudbutton.dobj.types;

public class Factory {
    public AbstractCounter createDegradableCounter() { return new DegradableCounter(); }
    public AbstractCounter createCounter() { return new Counter(); }
    public AbstractList createDegradableList() { return new DegradableList(); }
    public AbstractList createList() {return new List(); }
    public AbstractSet createDegradableSet() { return new DegradableSet(); }
    public AbstractSet createSet() { return new Set(); }

    public AbstractCounterSnapshot<Counter> createCounterSnapshot() { return new CounterSnapshot(); }
    public AbstractCounterSnapshot<DegradableCounter> createDegradableCounterSnapshot() { return new DegradableCounterSnapshot(); }
    public AbstractSetSnapshot<Set> createSetSnapshot () { return new SetSnapshot(); }
    public AbstractSetSnapshot<DegradableSet> createDegradableSetSnapshot () { return new DegradableSetSnapshot(); }
    public AbstractListSnapshot<List> createListSnapshot () { return  new ListSnapshot(); }
    public AbstractListSnapshot<DegradableList> createDegradableListSnapshot () { return  new DegradableListSnapshot(); }
}
