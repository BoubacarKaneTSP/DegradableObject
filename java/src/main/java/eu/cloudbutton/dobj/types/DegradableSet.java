package eu.cloudbutton.dobj.types;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class DegradableSet extends AbstractSet {

    private final ConcurrentMap<String, ConcurrentSkipListSet<String>> set;
    private final ThreadLocal<ConcurrentSkipListSet<String>> local;

    public DegradableSet() {
        this.set = new ConcurrentHashMap<>();
        this.local = new ThreadLocal<>();
    }

    @Override
    public void add(String s) {
        String pid = Thread.currentThread().getName();
        if(!set.containsKey(pid)) {
            local.set(new ConcurrentSkipListSet<>());
            this.set.put(pid, local.get());
        }
        local.get().add(s);
    }

    @Override
    public java.util.Set<String> read() {
        java.util.Set<String> result = new HashSet<>();
        for (ConcurrentSkipListSet<String> s : set.values()){
            result.addAll(s);
        }
        return result;
    }

    @Override
    public void remove(String s) {
        throw new java.lang.Error("Remove not build yet");
    }
}
