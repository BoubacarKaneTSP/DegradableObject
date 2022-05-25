package eu.cloudbutton.dobj.types;

import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

/**
 * This class provide a List.
 * WIP.
 *
 * @author Boubacar Kane
 * */
public class DegradableList<T> extends AbstractQueue<T> implements Queue<T> {

    private final ConcurrentMap<Thread, ConcurrentLinkedQueue<T>> list;
    private final ThreadLocal<ConcurrentLinkedQueue<T>> local;

    /**
     * Create an empty List.
     */
    public DegradableList() {
        this.list = new ConcurrentHashMap<>();
        this.local = ThreadLocal.withInitial(() -> {
            ConcurrentLinkedQueue<T> l = new ConcurrentLinkedQueue<>();
            list.put(Thread.currentThread(),l);
            return l;
        });    }

    /**
     * Returns a java.util.List that contains all elements
     * @return all elements stored in the object.
     */
    public List<T> read() {
        List<T> result = new ArrayList<>();
        for (ConcurrentLinkedQueue<T> val : list.values()){
            result.addAll(val);
        }
        return result;
    }

    /**
     * Removes a single instance of the specified element from this List.
     * Each process can only delete an element that it has previously added.
     * @param o
     * @return true if the element has been removed.
     */
    @Override
    public boolean remove(Object o) {
        return local.get().remove(o);
    }

    /**
     * Returns true if this List contains the specified element.
     * @param o
     * @return true if this List contains the specified element.
     */
    @Override
    public boolean contains(Object o) {

        boolean contained = false;

        for (ConcurrentLinkedQueue<T> s : list.values()){
            contained = s.contains(o);
            if (contained)
                break;
        }
        return contained;
    }

    public void clear(){
        for (ConcurrentLinkedQueue<T> list : list.values()){
            list.clear();
        }
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    /**
     * Returns an iterator over the elements in this list in proper sequence.
     * @return an iterator over the elements in this list in proper sequence.
     */
    @Override
    public Iterator<T> iterator() {

        List<T> result = new ArrayList<>();
        for (ConcurrentLinkedQueue<T> val : list.values()){
            result.addAll(val);
        }
        return result.iterator();
    }

    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return null;
    }

    /**
     * Appends the specified element to this list.
     * @param element element to be appended to this list
     */
    @Override
    public boolean add(T element) {
        return local.get().add(element);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return false;
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
    public String toString(){
        return "method toString not build yet";
    }
}
