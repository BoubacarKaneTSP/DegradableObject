package eu.cloudbutton.dobj.types;

import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class AbstractList {
    public abstract void append(String s);
    public abstract ConcurrentLinkedQueue<String> read();
    public abstract void remove(String s);
}
