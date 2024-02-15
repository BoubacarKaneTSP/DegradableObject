package eu.cloudbutton.dobj.types;

public abstract class AbstractSet<T> {
    public abstract void add(T val);
    public abstract java.util.Set<T> read();

    /**
     * Each process can only delete a value that it has previously written.
     *
     * @param val value to remove.
     * @return true if val has been removed.
     */
    public abstract boolean remove(T val);
    public abstract boolean contains(T val);
}
