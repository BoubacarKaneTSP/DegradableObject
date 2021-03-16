package eu.cloudbutton.dobj.types;

import java.util.concurrent.ConcurrentLinkedQueue;

public class List extends AbstractList {

    private final ConcurrentLinkedQueue<String> list;

    public List() {
        list = new ConcurrentLinkedQueue<>();
    }

    @Override
    public void append(String s) {
        list.add(s);
    }

    @Override
    public ConcurrentLinkedQueue<String> read() {
        return list;
    }

    @Override
    public void remove(String s) {
        list.remove(s);
    }
}
