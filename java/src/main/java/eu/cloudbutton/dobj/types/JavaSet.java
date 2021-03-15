package eu.cloudbutton.dobj.types;

import java.util.concurrent.ConcurrentSkipListSet;

public class JavaSet extends Set{

    private final ConcurrentSkipListSet<String> set;

    public JavaSet() {
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
