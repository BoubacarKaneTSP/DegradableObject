package eu.cloudbutton.dobj.sharded;

import eu.cloudbutton.dobj.utils.FactoryIndice;
import eu.cloudbutton.dobj.utils.BaseSegmentation;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ShardedLinkedList<E> extends BaseSegmentation<LinkedList> implements List<E> {

    public ShardedLinkedList(FactoryIndice factoryIndice) {
        super(LinkedList.class, factoryIndice);
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
    public boolean addAll(int index, @NotNull Collection<? extends E> c) {
        return false;
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

    @Override
    public E get(int index) {
        return null;
    }

    @Override
    public E set(int index, E element) {
        return null;
    }

    @Override
    public void add(int index, E element) {

    }

    @Override
    public E remove(int index) {
        return null;
    }

    @Override
    public int indexOf(Object o) {
        return 0;
    }

    @Override
    public int lastIndexOf(Object o) {
        return 0;
    }

    @NotNull
    @Override
    public ListIterator<E> listIterator() {
        return null;
    }

    @NotNull
    @Override
    public ListIterator<E> listIterator(int index) {
        return null;
    }

    @NotNull
    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        return null;
    }

}
