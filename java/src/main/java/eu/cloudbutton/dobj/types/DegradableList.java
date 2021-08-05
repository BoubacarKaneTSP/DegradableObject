package eu.cloudbutton.dobj.types;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

public class DegradableList<T> extends AbstractList<T> {

    private final ConcurrentMap<Thread, ConcurrentLinkedQueue<T>> list;
    private final ThreadLocal<ConcurrentLinkedQueue<T>> local;

    public DegradableList() {
        this.list = new ConcurrentHashMap<>();
        this.local = ThreadLocal.withInitial(() -> {
            ConcurrentLinkedQueue<T> l = new ConcurrentLinkedQueue<>();
            list.put(Thread.currentThread(),l);
            return l;
        });    }

    @Override
    public void append(T val) {
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
        return local.get().remove(val);
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

    public void clear(){
        for (ConcurrentLinkedQueue<T> list : list.values()){
            list.clear();
        }
    }

}
