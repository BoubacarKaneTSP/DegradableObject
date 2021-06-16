package eu.cloudbutton.dobj.types;

import java.util.List;

public abstract class AbstractList<T> {
    public abstract void append(T val);
    public abstract List<T> read();
    public abstract boolean remove(T val);
    public abstract boolean contains(T val);
}
