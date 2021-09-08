package eu.cloudbutton.dobj.types;

import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DegradableLinkedList<T> extends AbstractQueue<T> implements Queue<T> {

    private final ConcurrentMap<Thread, LinkedList<T>> list;
    private final ThreadLocal<LinkedList<T>> local;

    public DegradableLinkedList() {
        this.list = new ConcurrentHashMap<>();
        this.local = ThreadLocal.withInitial(() -> {
            LinkedList<T> l = new LinkedList<>();
            list.put(Thread.currentThread(), l);
            return l;
        });
    }

    @Override
    public Iterator<T> iterator() {
        return null;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean add(T val) {
        return local.get().add(val);
    }

    public List<T> read() {
        List<T> result = new ArrayList<>();
        for (LinkedList<T> val : list.values()){
            result.addAll(val.read());
        }
        return result;
    }

    @Override
    public boolean remove(Object val) {
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
    public boolean contains(Object val) {
        boolean contained = false;

        for (LinkedList<T> l : list.values()){
            contained = l.contains(val);
            if (contained) {
                break;
            }
        }
        return contained;
    }

    @Override
    public void clear() {
        throw new java.lang.Error("Remove not build yet");
    }

    @Override
    public boolean offer(T t) {
        return false;
    }

    @Override
    public T poll() {
        return null;
    }

    @Override
    public T peek() {
        return null;
    }

    @Override
    public String toString(){
        return "method toString not build yet";
    }
}
