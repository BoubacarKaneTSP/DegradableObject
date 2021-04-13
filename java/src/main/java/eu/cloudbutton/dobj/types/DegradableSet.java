package eu.cloudbutton.dobj.types;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class DegradableSet<T> extends AbstractSet<T> {

    private final ConcurrentMap<String, ConcurrentSkipListSet<T>> set;
    private final ThreadLocal<ConcurrentSkipListSet<T>> local;

    public DegradableSet() {
        this.set = new ConcurrentHashMap<>();
        this.local = new ThreadLocal<>();
    }

    @Override
    public void add(T val) {
        String pid = Thread.currentThread().getName();
        if(!set.containsKey(pid)) {
            local.set(new ConcurrentSkipListSet<>());
            this.set.put(pid, local.get());
        }
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
    public void remove(T val) {
        throw new java.lang.Error("Remove not build yet");
    }
}
