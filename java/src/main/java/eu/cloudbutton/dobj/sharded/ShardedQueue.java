package eu.cloudbutton.dobj.sharded;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ShardedQueue<E> implements Queue<E> {

    private final ThreadLocal<Queue<E>> local;

    public ShardedQueue(){
        local = ThreadLocal.withInitial(() -> {
            Queue<E> queue = new LinkedList();
            return queue;
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
    public Iterator<E> iterator() {
        return local.get().iterator();
    }

    @NotNull
    @Override
    public Object[] toArray() {
        return local.get().toArray();
    }

    @NotNull
    @Override
    public <T> T[] toArray(@NotNull T[] a) {
        return local.get().toArray(a);
    }

    @Override
    public boolean add(E e) {
        return local.get().add(e);
    }

    @Override
    public boolean remove(Object o) {
        return local.get().remove(o);
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return local.get().containsAll(c);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends E> c) {
        return local.get().addAll(c);
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        return local.get().removeAll(c);
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        return local.get().retainAll(c);
    }

    @Override
    public void clear() {
        local.get().clear();
    }

    @Override
    public boolean offer(E e) {
        return local.get().offer(e);
    }

    @Override
    public E remove() {
        return local.get().remove();
    }

    @Override
    public E poll() {
        return local.get().poll();
    }

    @Override
    public E element() {
        return local.get().element();
    }

    @Override
    public E peek() {
        return local.get().peek();
    }
}
