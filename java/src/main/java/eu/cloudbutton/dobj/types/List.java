package eu.cloudbutton.dobj.types;

import java.util.ArrayList;
import java.util.Arrays;
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
    public java.util.List<T> read() {

        java.util.List result = new ArrayList();

        result = Arrays.asList(list.toArray());

        return result;
    }

    @Override
    public void remove(T val) {
        list.remove(val);
    }
}
