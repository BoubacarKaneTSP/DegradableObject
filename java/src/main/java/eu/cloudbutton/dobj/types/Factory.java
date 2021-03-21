package eu.cloudbutton.dobj.types;

public class Factory {
    public AbstractCounter createDegradableCounter() { return new DegradableCounter(); }
    public AbstractCounter createCounter() { return new Counter(); }
    public AbstractList createDegradableList() { return new DegradableList(); }
    public AbstractList createList() {return new List(); }
    public AbstractSet createDegradableSet() { return new DegradableSet(); }
    public AbstractSet createSet() { return new Set(); }
}
