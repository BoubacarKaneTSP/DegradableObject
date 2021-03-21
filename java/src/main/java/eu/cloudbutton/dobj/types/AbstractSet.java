package eu.cloudbutton.dobj.types;

public abstract class AbstractSet {
    public abstract void add(String s);
    public abstract java.util.Set<String> read();
    public abstract void remove(String s);
}
