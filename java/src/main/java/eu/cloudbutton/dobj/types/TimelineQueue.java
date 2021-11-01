package eu.cloudbutton.dobj.types;

import java.util.AbstractCollection;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class TimelineQueue<T> extends AbstractCollection<T> implements Deque<T> {

    private AtomicInteger head, tail;
    private ConcurrentHashMap<Integer, T> map;

    public TimelineQueue() {
        this.head = new AtomicInteger(0);
        this.tail = new AtomicInteger(0);
        this.map = new ConcurrentHashMap<>();
    }

    @Override
    public Iterator<T> iterator() {
        return null;
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
        return 0;
    }

    @Override
    public boolean offer(T t) {
        return false;
    }

    @Override
    public T remove() {
        return null;
    }

    @Override
    public T poll() {
        return null;
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
