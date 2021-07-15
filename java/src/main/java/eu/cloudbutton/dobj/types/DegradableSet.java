package eu.cloudbutton.dobj.types;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class DegradableSet<T> extends AbstractSet<T> {

    private final ConcurrentMap<Thread, ConcurrentSkipListSet<T>> set;
    private final ThreadLocal<ConcurrentSkipListSet<T>> local;

    public DegradableSet() {
        this.set = new ConcurrentHashMap<>();
        this.local = ThreadLocal.withInitial(() -> {
            ConcurrentSkipListSet<T> l = new ConcurrentSkipListSet<>();
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
        for (ConcurrentSkipListSet<T> val : set.values()){
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
        for (ConcurrentSkipListSet<T> s : set.values()){
            if (s.contains(val)) return true;
        }
        return false;
    }
}
