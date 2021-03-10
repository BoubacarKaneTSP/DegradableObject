package factories;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public class javaset extends set {

    private final ConcurrentSkipListSet<String> set;

    public javaset() {
        set = new ConcurrentSkipListSet<>();
    }

    @Override
    public void add(String s) {
        set.add(s);
    }

    @Override
    public Set<String> read() {
        return set;
    }

    @Override
    public void remove(String s) {
        set.remove(s);
    }

    @Override
    public void write(String s) {
        write(s);
    }
}
