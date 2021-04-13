package eu.cloudbutton.dobj.types;

import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class AbstractList<T> {
    public abstract void append(T val);
    public abstract ConcurrentLinkedQueue<T> read();
    public abstract void remove(T val);
}
