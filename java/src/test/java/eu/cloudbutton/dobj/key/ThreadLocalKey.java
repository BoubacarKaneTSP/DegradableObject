package eu.cloudbutton.dobj.key;

import com.fasterxml.uuid.Generators;
import eu.cloudbutton.dobj.utils.BaseSegmentable;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public class ThreadLocalKey extends BaseSegmentable implements Key, Comparable<ThreadLocalKey>{

    public UUID id;

    public ThreadLocalKey(long tid, long id) {
        this.id = UUID.randomUUID();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ThreadLocalKey that = (ThreadLocalKey) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public int compareTo(@NotNull ThreadLocalKey key) {
        return id.compareTo(key.id);
    }

    @Override
    public String toString() {
        return "("+id+")";
    }

}
