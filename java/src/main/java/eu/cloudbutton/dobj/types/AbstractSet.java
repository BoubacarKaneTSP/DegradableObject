package eu.cloudbutton.dobj.types;

public abstract class AbstractSet<T> {
    public abstract void add(T val);
    public abstract java.util.Set<T> read();
    public abstract void remove(T val);
}
