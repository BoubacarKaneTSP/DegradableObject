package eu.cloudbutton.dobj.key;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Random;

public class ThreadLocalKey implements Key, Comparable<ThreadLocalKey>{

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

    @Override
    public int compareTo(@NotNull ThreadLocalKey key) {
        if (id>key.id) return 1;
        else if (key.id>id) return -1;
        else if (tid>key.tid) return 1;
        else if (tid<key.tid) return 1;
        return 0;
    }
}
