package eu.cloudbutton.dobj.types;

import java.util.HashSet;
import java.util.concurrent.ConcurrentSkipListSet;

public class Set<T> extends AbstractSet<T> {

    private final java.util.Set<T> set;

    public Set() {
        set = new HashSet<>();
    }

    @Override
    public void add(T val) {
        set.add(val);
    }

    @Override
    public java.util.Set<T> read() {
        return set;
    }

    @Override
    public boolean remove(T s) {
        return set.remove(s);
    }

    @Override
    public boolean contains(T val) {
        return set.contains(val);
    }

}
