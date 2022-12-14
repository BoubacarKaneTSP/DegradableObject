package eu.cloudbutton.dobj.segmented;

import eu.cloudbutton.dobj.utils.Segmentation;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class SegmentedLinkedListSet<E> implements Set<E> {

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean contains(Object o) {
        return false;
    }

    @NotNull
    @Override
    public Iterator<E> iterator() {
        return null;
    }

    @NotNull
    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @NotNull
    @Override
    public <T> T[] toArray(@NotNull T[] ts) {
        return null;
    }

    @Override
    public boolean add(E e) {
        return false;
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> collection) {
        return false;
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends E> collection) {
        return false;
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> collection) {
        return false;
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> collection) {
        return false;
    }

    @Override
    public void clear() {

    }
}
