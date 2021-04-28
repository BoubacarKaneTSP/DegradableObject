package eu.cloudbutton.dobj.types;

public class Factory {
    public AbstractCounter createDegradableCounter() { return new DegradableCounter(); }
    public AbstractCounter createCounter() { return new Counter(); }
    public AbstractList createDegradableList() { return new DegradableList(); }
    public AbstractList createList() {return new List(); }
    public AbstractSet createDegradableSet() { return new DegradableSet(); }
    public AbstractSet createSet() { return new Set(); }

    public AbstractCounter createCounterSnapshot() { return new CounterSnapshot(); }
    public AbstractSet createSetSnapshot () { return new SetSnapshot(); }
    public AbstractList createListSnapshot () { return  new ListSnapshot(); }

    public AbstractList createSecondDegradableList() {return new SecondDegradableList(); }
    public AbstractList createThirdDegradableList() {return new ThirdDegradableList(); }
}
