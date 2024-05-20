package eu.cloudbutton.dobj.key;

import eu.cloudbutton.dobj.utils.BaseSegmentable;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public class SimpleKey extends BaseSegmentable implements Key, Comparable<SimpleKey>{

    public UUID id;

    public SimpleKey(long id) {
        this.id = UUID.randomUUID();
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
        return Objects.hash(id); // % 100_000;
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
