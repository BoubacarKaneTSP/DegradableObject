package eu.cloudbutton.dobj.sharded;

import eu.cloudbutton.dobj.utils.BaseSegmentation;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ShardedHashSet<E> extends BaseSegmentation<HashSet> implements Set<E> {

    public ShardedHashSet(){
        super(HashSet.class);
    }

    @Override
    public int size() {
        int ret = 0;
        for(HashSet<E> set: segments) {
            ret+=set.size();
        }
        return ret;
    }

    @Override
    public boolean isEmpty() {
        throw new IllegalStateException("not supported");
    }

    @Override
    public boolean contains(Object o) {
        throw new IllegalStateException("not supported");
    }

    @NotNull
    @Override
    public Iterator<E> iterator() {
        return segmentFor(null).iterator();
    }

    @NotNull
    @Override
    public Object[] toArray() {
        throw new IllegalStateException("not supported");
    }

    @NotNull
    @Override
    public <T> T[] toArray(@NotNull T[] ts) {
        throw new IllegalStateException("not supported");
    }

    @Override
    public boolean add(E e) {
        return segmentFor(e).add(e);
    }

    @Override
    public boolean remove(Object o) {
        return segmentFor(o).remove(o);
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> collection) {
        throw new IllegalStateException("not supported");
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends E> collection) {
        throw new IllegalStateException("not supported");
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> collection) {
        throw new IllegalStateException("not supported");
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> collection) {
        throw new IllegalStateException("not supported");
    }

    @Override
    public void clear() {
        throw new IllegalStateException("not supported");
    }
}
