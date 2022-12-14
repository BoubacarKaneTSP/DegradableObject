package eu.cloudbutton.dobj.sharded;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ShardedSet<E> implements Set<E> {

    private final ThreadLocal<Set<E>> local;

    public ShardedSet(){
        local = ThreadLocal.withInitial(() -> {
            Set<E> set = new HashSet<>();
            return set;
        });
    }

    @Override
    public int size() {
        return local.get().size();
    }

    @Override
    public boolean isEmpty() {
        return local.get().isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return local.get().contains(o);
    }

    @NotNull
    @Override
    public Iterator iterator() {
        return local.get().iterator();
    }

    @NotNull
    @Override
    public Object[] toArray() {
        return local.get().toArray();
    }

    @Override
    public boolean add(E o) {
        return local.get().add(o);
    }

    @Override
    public boolean remove(Object o) {
        return local.get().remove(o);
    }

    @Override
    public boolean addAll(@NotNull Collection c) {
        return local.get().addAll(c);
    }

    @Override
    public void clear() {
        local.get().clear();
    }

    @Override
    public boolean removeAll(@NotNull Collection c) {
        return local.get().removeAll(c);
    }

    @Override
    public boolean retainAll(@NotNull Collection c) {
        return local.get().retainAll(c);
    }

    @Override
    public boolean containsAll(@NotNull Collection c) {
        return local.get().containsAll(c);
    }

    @NotNull
    @Override
    public Object[] toArray(@NotNull Object[] a) {
        return local.get().toArray(a);
    }
}
