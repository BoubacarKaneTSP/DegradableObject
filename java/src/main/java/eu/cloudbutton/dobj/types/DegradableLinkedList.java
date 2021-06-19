package eu.cloudbutton.dobj.types;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DegradableLinkedList<T> extends AbstractList<T>{

    private final ConcurrentMap<Integer, LinkedList<T>> list;
    private final ThreadLocal<LinkedList<T>> local;
    private final ThreadLocal<Integer> name;

    public DegradableLinkedList() {
        this.list = new ConcurrentHashMap<>();
        this.local = new ThreadLocal<>();
        name = new ThreadLocal<>();
    }

    @Override
    public void append(T val) {
        if (name.get() == null)
            name.set(Integer.parseInt(Thread.currentThread().getName().substring(5).replace("-thread-","")));

        if(!list.containsKey(name.get())){
            local.set(new LinkedList<>());
            this.list.put(name.get(), local.get());
        }
        local.get().append(val);
    }

    @Override
    public List<T> read() {
        List<T> result = new ArrayList<>();
        for (LinkedList<T> val : list.values()){
            result.addAll(val.read());
        }
        return result;
    }

    @Override
    public boolean remove(T val) {
        boolean removed = false;

        for (LinkedList<T> l : list.values()){
            removed = l.remove(val);
            if (removed) {
                break;
            }
        }
        return removed;
    }

    @Override
    public boolean contains(T val) {
        boolean contained = false;

        for (LinkedList<T> l : list.values()){
            contained = l.contains(val);
            if (contained) {
                break;
            }
        }
        return contained;
    }
}
