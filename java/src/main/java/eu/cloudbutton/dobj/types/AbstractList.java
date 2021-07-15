package eu.cloudbutton.dobj.types;

import java.util.List;

public abstract class AbstractList<T> {
    public abstract void append(T val);
    public abstract List<T> read();
    /**
     * Each process can only delete a value that it has previously written.
     * It removes only one occurrence.
     * @param val value to remove.
     * @return true if val has been removed.
     */
    public abstract boolean remove(T val);
    public abstract boolean contains(T val);
}
