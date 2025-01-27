package eu.cloudbutton.dobj.key;

import eu.cloudbutton.dobj.utils.BaseSegmentable;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SimpleKey extends BaseSegmentable implements Key, Comparable<SimpleKey>{

    public String id;

    public SimpleKey(long tid, long id) {
        this.id = "user:"+tid+":"+id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleKey that = (SimpleKey) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public int compareTo(@NotNull SimpleKey other) {
        return id.compareTo(other.id);
    }

    @Override
    public String toString() {
        return "("+id+")";
    }

}
