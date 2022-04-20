package eu.cloudbutton.dobj.Set;

import java.util.AbstractSet;
import java.util.HashSet;
import java.util.Iterator;
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
    public Iterator<T> iterator() {
        AbstractSet<T> iteratorSet = new HashSet<>();

        for (ConcurrentSkipListSet<T> s: set.values()){
            iteratorSet.addAll(s);
        }
        return iteratorSet.iterator();
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean add(T val) {
        return local.get().add(val);
    }

    public java.util.Set<T> read() {
        java.util.Set<T> result = new HashSet<>();
        for (ConcurrentSkipListSet<T> val : set.values()){
            result.addAll(val);
        }
        return result;
    }

    @Override
    public boolean remove(Object val) {
        return local.get().remove(val);
    }

    @Override
    public boolean contains(Object val) {
        for (ConcurrentSkipListSet<T> s : set.values()){
            if (s.contains(val)) return true;
        }
        return false;
    }

    @Override
    public String toString(){
        return "method toString not build yet";
    }

}
