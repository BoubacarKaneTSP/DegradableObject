package eu.cloudbutton.dobj.types;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

public class DegradableList extends AbstractList {

    private final ConcurrentMap<String, ConcurrentLinkedQueue<String>> list;
    private final ThreadLocal<ConcurrentLinkedQueue<String>> local;

    public DegradableList() {
        this.list = new ConcurrentHashMap<>();
        this.local = new ThreadLocal<>();
    }

    @Override
    public void append(String s) {
        String pid = Thread.currentThread().getName();
        if(!list.containsKey(pid)){
            local.set(new ConcurrentLinkedQueue<>());
            this.list.put(pid, local.get());
        }
        local.get().add(s);
    }

    @Override
    public ConcurrentLinkedQueue<String> read() {
        ConcurrentLinkedQueue<String> result = new ConcurrentLinkedQueue<>();
        for (ConcurrentLinkedQueue<String> s : list.values()){
            result.addAll(s);
        }
        return result;
    }

    @Override
    public void remove(String s) {
        throw new java.lang.Error("Remove not build yet");
    }
}
