package eu.cloudbutton.dobj.types;

import java.util.concurrent.ConcurrentLinkedQueue;

public class List<T> extends AbstractList<T> {

    private final ConcurrentLinkedQueue<T> list;

    public List() {
        list = new ConcurrentLinkedQueue<>();
    }

    @Override
    public void append(T val) {
        list.add(val);
    }

    @Override
    public ConcurrentLinkedQueue<T> read() {
        return list;
    }

    @Override
    public void remove(T val) {
        list.remove(val);
    }
}
