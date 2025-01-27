package eu.cloudbutton.dobj.sharded;

import eu.cloudbutton.dobj.utils.BaseSegmentation;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ShardedLinkedList<E> extends BaseSegmentation<LinkedList> implements Queue<E> {

    public ShardedLinkedList() {
        super(LinkedList.class);
    }


    @Override
    public int size() {
        throw new IllegalStateException("not supported");
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
        throw new IllegalStateException("not supported");
    }

    @NotNull
    @Override
    public Object[] toArray() {
        throw new IllegalStateException("not supported");
    }

    @NotNull
    @Override
    public <T> T[] toArray(@NotNull T[] a) {
        throw new IllegalStateException("not supported");
    }

    @Override
    public boolean add(E e) {
        return segmentFor(e).add(e);
    }

    @Override
    public boolean offer(E e) {
        return segmentFor(e).offer(e);
    }

    @Override
    public E remove() {
        return null;
    }

    @Override
    public E poll() {
        return null;
    }

    @Override
    public E element() {
        return null;
    }

    @Override
    public E peek() {
        return null;
    }

    @Override
    public boolean remove(Object o) {
        return segmentFor(o).remove(o);
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        throw new IllegalStateException("not supported");
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends E> c) {
        throw new IllegalStateException("not supported");
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        throw new IllegalStateException("not supported");
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        throw new IllegalStateException("not supported");
    }

    @Override
    public void clear() {
        segmentFor(null).clear();
    }

}
