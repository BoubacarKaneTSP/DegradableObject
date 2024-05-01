package eu.cloudbutton.dobj.key;

import eu.cloudbutton.dobj.utils.SegmentAware;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public class ThreadLocalKey extends SegmentAware implements Key, Comparable<ThreadLocalKey>{

    public long tid;
    public String id;

    public ThreadLocalKey(long tid, long id) {
        this.tid = tid;
        this.id = UUID.randomUUID().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ThreadLocalKey that = (ThreadLocalKey) o;
        return tid == that.tid && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tid, id);
    }

    @Override
    public int compareTo(@NotNull ThreadLocalKey key) {
        int ret = id.compareTo(key.id);
        if (ret == 0) {
            ret = Long.compare(tid, key.tid);
        }
        return ret;
    }

    @Override
    public String toString() {
        return "("+id+","+tid+")";
    }
}
