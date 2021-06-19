package eu.cloudbutton.dobj.types;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class DegradableSet<T> extends AbstractSet<T> {

    private final ConcurrentMap<Integer, ConcurrentSkipListSet<T>> set;
    private final ThreadLocal<ConcurrentSkipListSet<T>> local;
    private final ThreadLocal<Integer> name;

    public DegradableSet() {
        this.set = new ConcurrentHashMap<>();
        this.local = new ThreadLocal<>();
        name = new ThreadLocal<>();
    }

    @Override
    public void add(T val) {
        if (name.get() == null)
            name.set(Integer.parseInt(Thread.currentThread().getName().substring(5).replace("-thread-","")));

        if(!set.containsKey(name.get())) {
            local.set(new ConcurrentSkipListSet<>());
            this.set.put(name.get(), local.get());
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
