package eu.cloudbutton.dobj.queue;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;

public class MapQueue<T> implements Queue<T>{

    private AtomicInteger head, tail;
    private ConcurrentSkipListMap<Integer, T> map;

    public MapQueue() {
        this.head = new AtomicInteger(0);
        this.tail = new AtomicInteger(0);
        this.map = new ConcurrentSkipListMap<>(Comparator.reverseOrder());
    }

    @Override
    public Iterator<T> iterator() {
        return map.values().iterator();
    }

    public void addFirst(T t) {
        this.map.put(head.getAndIncrement(), t);
    }

    public T removeLast() {

        while (head.get() - tail.get() > 0){
            int last = tail .get();
            if (tail.compareAndSet(last, last+1))
                return map.remove(last);
        }
        return null;
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean contains(Object o) {
        return false;
    }

    @Override
    public boolean add(T t) {
        return false;
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return false;
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends T> c) {
        return false;
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        return false;
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        return false;
    }

    @Override
    public void clear() {

    }

    @Override
    public boolean offer(T t) {
        addFirst(t);
        return true;
    }

    @Override
    public T remove() {
        return null;
    }

    @Override
    public T poll() {
        return removeLast();
    }

    @Override
    public T element() {
        return null;
    }

    @Override
    public T peek() {
        return null;
    }

    @Override
    public Object[] toArray(){
        return map.values().toArray();
    }

    @NotNull
    @Override
    public <T1> T1[] toArray(@NotNull T1[] a) {
        return null;
    }
}
