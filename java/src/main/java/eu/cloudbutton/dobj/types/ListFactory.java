package eu.cloudbutton.dobj.types;

public class ListFactory {
    public AbstractList createdegradablelist() { return new DegradableList(); }
    public AbstractList createjavalist() {return new List(); }
}
