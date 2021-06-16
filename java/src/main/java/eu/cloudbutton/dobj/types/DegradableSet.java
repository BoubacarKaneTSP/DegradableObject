package eu.cloudbutton.dobj.types;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class DegradableSet<T> extends AbstractSet<T> {

    private final ConcurrentMap<Integer, ConcurrentSkipListSet<T>> set;
    private final ThreadLocal<ConcurrentSkipListSet<T>> local;

    public DegradableSet() {
        this.set = new ConcurrentHashMap<>();
        this.local = new ThreadLocal<>();
    }

    @Override
    public void add(T val) {
        int pid = Integer.parseInt(Thread.currentThread().getName().substring(5).replace("-thread-",""));
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
    public boolean remove(T val) {

        return local.get().remove(val);
    }

    @Override
    public boolean contains(T val) {

        boolean contained = false;

        for (ConcurrentSkipListSet<T> s : set.values()){
            contained = s.contains(val);
            if (contained)
                break;
        }
        return contained;
    }
}
