package eu.cloudbutton.dobj.types;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class DegradableSet<T> extends AbstractSet<T> {

    private final ConcurrentMap<Thread, Set<T>> set;
    private final ThreadLocal<Set<T>> local;

    public DegradableSet() {
        this.set = new ConcurrentHashMap<>();
        this.local = ThreadLocal.withInitial(() -> {
            Set<T> l = new HashSet<>();
            set.put(Thread.currentThread(),l);
            return l;
        });
    }

    @Override
    public void add(T val) {
        local.get().add(val);
    }

    @Override
    public java.util.Set<T> read() {
        java.util.Set<T> result = new HashSet<>();
        for (Set<T> val : set.values()){
            result.addAll(val);
        }
        return result;
    }

    @Override
    public boolean remove(T val) {
        return local.get().remove(val);
    }

    @Override
    public boolean contains(T val) {
        for (Set<T> s : set.values()){
            if (s.contains(val)) return true;
        }
        return false;
    }
}
