package eu.cloudbutton.dobj.types;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class List<T> extends AbstractQueue<T> implements Queue<T> {

    private final ConcurrentLinkedQueue<T> list;

    public List() {
        list = new ConcurrentLinkedQueue<>();
    }

    @Override
    public Iterator<T> iterator() {
        return list.iterator();
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean add(T val) {
        return list.add(val);
    }

    public java.util.List<T> read() {

        java.util.List result;

        result = Arrays.asList(list.toArray());

        return result;
    }

    @Override
    public boolean remove(Object val) {
        return list.remove(val);
    }

    @Override
    public boolean contains(Object val) {
        return list.contains(val);
    }

    @Override
    public void clear() {
        list.clear();
    }

    @Override
    public boolean offer(T t) {
        return list.offer(t);
    }

    @Override
    public T poll() {
        return list.poll();
    }

    @Override
    public T peek() {
        return list.peek();
    }
}
