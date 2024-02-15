package eu.cloudbutton.dobj.types;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;

public class MapQueue<T> extends AbstractQueue<T> implements Deque<T> {

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

    @Override
    public Iterator<T> descendingIterator() {
        return null;
    }

    @Override
    public void addFirst(T t) {
        this.map.put(head.getAndIncrement(), t);
    }

    @Override
    public void addLast(T t) {
    }

    @Override
    public boolean offerFirst(T t) {
        return false;
    }

    @Override
    public boolean offerLast(T t) {
        return false;
    }

    @Override
    public T removeFirst() {
        return null;
    }

    @Override
    public T removeLast() {

        while (head.get() - tail.get() > 0){
            int last = tail .get();
            if (tail.compareAndSet(last, last+1))
                return map.remove(last);
        }
        return null;
    }

    @Override
    public T pollFirst() {
        return null;
    }

    @Override
    public T pollLast() {
        return null;
    }

    @Override
    public T getFirst() {
        return null;
    }

    @Override
    public T getLast() {
        return null;
    }

    @Override
    public T peekFirst() {
        return null;
    }

    @Override
    public T peekLast() {
        return null;
    }

    @Override
    public boolean removeFirstOccurrence(Object o) {
        return false;
    }

    @Override
    public boolean removeLastOccurrence(Object o) {
        return false;
    }

    @Override
    public void push(T t) {

    }

    @Override
    public T pop() {
        return null;
    }

    @Override
    public int size() {
        return map.size();
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
}
