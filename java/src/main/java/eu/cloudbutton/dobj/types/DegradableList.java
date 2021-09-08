package eu.cloudbutton.dobj.types;

import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

public class DegradableList<T> extends AbstractQueue<T> implements Queue<T> {

    private final ConcurrentMap<Thread, ConcurrentLinkedQueue<T>> list;
    private final ThreadLocal<ConcurrentLinkedQueue<T>> local;

    public DegradableList() {
        this.list = new ConcurrentHashMap<>();
        this.local = ThreadLocal.withInitial(() -> {
            ConcurrentLinkedQueue<T> l = new ConcurrentLinkedQueue<>();
            list.put(Thread.currentThread(),l);
            return l;
        });    }


    public List<T> read() {
        List<T> result = new ArrayList<>();
        for (ConcurrentLinkedQueue<T> val : list.values()){
            result.addAll(val);
        }
        return result;
    }


    @Override
    public boolean remove(Object val) {
        return local.get().remove(val);
    }

    @Override
    public boolean contains(Object val) {

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

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public Iterator<T> iterator() {
        return null;
    }

    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return null;
    }

    @Override
    public boolean add(T t) {
        return local.get().add(t);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean offer(T t) {
        return false;
    }

    @Override
    public T remove() {
        return null;
    }

    @Override
    public T poll() {
        return null;
    }

    @Override
    public T element() {
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
