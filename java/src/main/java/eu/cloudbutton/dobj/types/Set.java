package eu.cloudbutton.dobj.types;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.concurrent.ConcurrentSkipListSet;

public class Set<T> extends AbstractSet<T> {

    private final ConcurrentSkipListSet<T> set;

    public Set() {
        set = new ConcurrentSkipListSet<>();
    }

    @Override
    public Iterator<T> iterator() {
        return set.iterator();
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean add(T val) {
        set.add(val);
        return true;
    }

    public java.util.Set<T> read() {
        return set;
    }

    @Override
    public boolean remove(Object s) {
        return set.remove(s);
    }

    @Override
    public boolean contains(Object val) {
        return set.contains(val);
    }

}
