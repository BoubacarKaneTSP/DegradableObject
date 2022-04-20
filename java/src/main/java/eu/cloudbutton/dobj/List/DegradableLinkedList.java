package eu.cloudbutton.dobj.List;

import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * This class provide a LinkedList.
 * WIP.
 *
 * @author Boubacar Kane
 * */
public class DegradableLinkedList<T> extends AbstractList<T> {

    private final ConcurrentMap<Thread, LinkedList<T>> list;
    private final ThreadLocal<LinkedList<T>> local;

    /**
     * Create an empty LinkedList.
     */
    public DegradableLinkedList() {
        this.list = new ConcurrentHashMap<>();
        this.local = ThreadLocal.withInitial(() -> {
            LinkedList<T> l = new LinkedList<>();
            list.put(Thread.currentThread(), l);
            return l;
        });
    }

    @Override
    public int size() {
        return 0;
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
    public T get(int index) {
        return null;
    }

    /**
     * Returns a java.util.List that contains all elements
     * @return all elements stored in the object.
     */
    public List<T> read() {
        List<T> result = new ArrayList<>();
        for (LinkedList<T> val : list.values()){
            result.addAll(val.read());
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
        boolean removed = false;

        for (LinkedList<T> l : list.values()){
            removed = l.remove(o);
            if (removed) {
                break;
            }
        }
        return removed;
    }

    /**
     * Returns true if this List contains the specified element.
     * @param o
     * @return true if this List contains the specified element.
     */
    @Override
    public boolean contains(Object o) {
        boolean contained = false;

        for (LinkedList<T> l : list.values()){
            contained = l.contains(o);
            if (contained) {
                break;
            }
        }
        return contained;
    }

    /**
     * Removes all of the elements from this List.
     */
    @Override
    public void clear() {
        throw new java.lang.Error("clear not build yet");
    }

    /**
     * Returns a string representation of this List.
     * @return a string representation of this List.
     */
    @Override
    public String toString(){
        return "method toString not build yet";
    }


    /**
     * Returns an iterator over the elements in this list in proper sequence.
     * @return an iterator over the elements in this list in proper sequence.
     */
    @Override
    public Iterator<T> iterator() {
        List<T> result = new ArrayList<>();
        for (LinkedList<T> val : list.values()){
            result.addAll(val.read());
        }
        return result.iterator();
    }
}
