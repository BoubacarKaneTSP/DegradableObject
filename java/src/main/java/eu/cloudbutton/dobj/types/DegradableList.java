package eu.cloudbutton.dobj.types;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

public class DegradableList<T> extends AbstractList<T> {

    private final ConcurrentMap<Integer, ConcurrentLinkedQueue<T>> list;
    private final ThreadLocal<ConcurrentLinkedQueue<T>> local;

    public DegradableList() {
        this.list = new ConcurrentHashMap<>();
        this.local = new ThreadLocal<>();
    }

    @Override
    public void append(T val) {
        int pid = Integer.parseInt(Thread.currentThread().getName().substring(5).replace("-thread-",""));
        if(!list.containsKey(pid)){
            local.set(new ConcurrentLinkedQueue<>());
            this.list.put(pid, local.get());
        }
        local.get().add(val);
    }

    @Override
    public List<T> read() {
        List<T> result = new ArrayList<>();
        for (ConcurrentLinkedQueue<T> val : list.values()){
            result.addAll(val);
        }
        return result;
    }

    @Override
    public boolean remove(T val) {
        throw new java.lang.Error("Remove not build yet");
    }

    @Override
    public boolean contains(T val) {

        boolean contained = false;

        for (ConcurrentLinkedQueue<T> s : list.values()){
            contained = s.contains(val);
            if (contained)
                break;
        }
        return contained;
    }
}
