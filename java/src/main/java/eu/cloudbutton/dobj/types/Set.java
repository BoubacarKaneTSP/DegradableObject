package eu.cloudbutton.dobj.types;

import java.util.concurrent.ConcurrentSkipListSet;

public class Set extends AbstractSet {

    private final ConcurrentSkipListSet<String> set;

    public Set() {
        set = new ConcurrentSkipListSet<>();
    }

    @Override
    public void add(String s) {
        set.add(s);
    }

    @Override
    public java.util.Set<String> read() {
        return set;
    }

    @Override
    public void remove(String s) {
        set.remove(s);
    }
}
