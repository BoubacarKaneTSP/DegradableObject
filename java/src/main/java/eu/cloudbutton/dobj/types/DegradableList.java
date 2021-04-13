package eu.cloudbutton.dobj.types;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

public class DegradableList<T> extends AbstractList<T> {

    private final ConcurrentMap<String, ConcurrentLinkedQueue<T>> list;
    private final ThreadLocal<ConcurrentLinkedQueue<T>> local;

    public DegradableList() {
        this.list = new ConcurrentHashMap<>();
        this.local = new ThreadLocal<>();
    }

    @Override
    public void append(T val) {
        String pid = Thread.currentThread().getName();
        if(!list.containsKey(pid)){
            local.set(new ConcurrentLinkedQueue<>());
            this.list.put(pid, local.get());
        }
        local.get().add(val);
    }

    @Override
    public ConcurrentLinkedQueue<T> read() {
        ConcurrentLinkedQueue<T> result = new ConcurrentLinkedQueue<>();
        for (ConcurrentLinkedQueue<T> val : list.values()){
            result.addAll(val);
        }
        return result;
    }

    @Override
    public void remove(T val) {
        throw new java.lang.Error("Remove not build yet");
    }
}
