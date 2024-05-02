package eu.cloudbutton.dobj.key;

import com.fasterxml.uuid.Generators;
import eu.cloudbutton.dobj.juc.ThreadLocalRandom;
import eu.cloudbutton.dobj.utils.SegmentAware;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public class ThreadLocalKey extends SegmentAware implements Key, Comparable<ThreadLocalKey>{

    public UUID id;
    public int hash;

    public ThreadLocalKey(long tid, long id) {
        this.id = Generators.timeBasedGenerator().generate();
        this.hash = ThreadLocalRandom.current().nextInt(32768);
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
        // return Objects.hash(id);
        return hash;
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
