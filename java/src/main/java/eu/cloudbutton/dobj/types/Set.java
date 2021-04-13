package eu.cloudbutton.dobj.types;

import java.util.concurrent.ConcurrentSkipListSet;

public class Set<T> extends AbstractSet<T> {

    private final ConcurrentSkipListSet<T> set;

    public Set() {
        set = new ConcurrentSkipListSet<>();
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
    public void remove(T s) {
        set.remove(s);
    }
}
