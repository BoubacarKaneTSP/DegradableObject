package eu.cloudbutton.dobj.types;

public class Factory {
    public DegradableCounter createDegradableCounter() { return new DegradableCounter(); }
    public Counter createCounter() { return new JavaCounter(); }
    public AbstractList createDegradableList() { return new DegradableList(); }
    public AbstractList createList() {return new List(); }
    public Set createDegradableSet() { return new DegradableSet(); }
    public Set createSet() { return new JavaSet(); }
}
