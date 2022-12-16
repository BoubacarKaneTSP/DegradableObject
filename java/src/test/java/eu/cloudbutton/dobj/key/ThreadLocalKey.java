package eu.cloudbutton.dobj.key;

import java.util.Objects;
import java.util.Random;

public class ThreadLocalKey implements Key{

    public long tid;
    public long id;

    public ThreadLocalKey(long tid, long id) {
        this.tid = tid;
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ThreadLocalKey that = (ThreadLocalKey) o;
        return tid == that.tid && id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tid, id);
    }
}
