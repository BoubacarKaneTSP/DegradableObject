package eu.cloudbutton.dobj.list;

import org.jetbrains.annotations.NotNull;

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
public class DegradableLinkedList<T> implements List<T> {

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

    @Override
    public boolean isEmpty() {
        return false;
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

    @Override
    public T set(int index, T element) {
        return null;
    }

    @Override
    public void add(int index, T element) {

    }

    @Override
    public T remove(int index) {
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
    public ListIterator<T> listIterator() {
        return null;
    }

    @NotNull
    @Override
    public ListIterator<T> listIterator(int index) {
        return null;
    }

    @NotNull
    @Override
    public List<T> subList(int fromIndex, int toIndex) {
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

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return false;
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends T> c) {
        return false;
    }

    @Override
    public boolean addAll(int index, @NotNull Collection<? extends T> c) {
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

    @NotNull
    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @NotNull
    @Override
    public <T1> T1[] toArray(@NotNull T1[] a) {
        return null;
    }
}
