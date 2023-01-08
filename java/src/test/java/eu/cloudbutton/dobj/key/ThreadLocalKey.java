package eu.cloudbutton.dobj.key;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ThreadLocalKey implements Key, Comparable<ThreadLocalKey>{

    public long tid;
    public long id;

    public ThreadLocalKey(long tid, long id, int max_key_per_thread) {
        this.tid = tid;
        this.id = id% max_key_per_thread;
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
        int hash = Objects.hash(tid, id);
        hash = hash < 0 ? hash * -1 : hash;
        return hash;
    }

    @Override
    public int compareTo(@NotNull ThreadLocalKey key) {
        if (id>key.id) return 1;
        else if (id< key.id) return -1;
        else if (tid>key.tid) return 1;
        else if (tid<key.tid) return -1;
        return 0;
    }

    @Override
    public String toString() {
        return "("+id+","+tid+")";
    }
}
