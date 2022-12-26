package eu.cloudbutton.dobj.swsr;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class SWSRSkipListSet<E extends Comparable<E>> extends AbstractSet<E> {

    private final SortedMap m;

    public SWSRSkipListSet() {
        m = new SWSRSkipListMap<E,Object>();
    }

    public SWSRSkipListSet(Collection<? extends E> c) {
        m =  new SWSRSkipListMap<E,Object>();
        addAll(c);
    }

    /**
     * Returns <tt>true</tt> if this set contains no elements.
     * @return <tt>true</tt> if this set contains no elements
     */
    public boolean isEmpty() {
        return m.isEmpty();
    }

    /**
     * Returns <tt>true</tt> if this set contains the specified element.
     * More formally, returns <tt>true</tt> if and only if this set
     * contains an element <tt>e</tt> such that <tt>o.equals(e)</tt>.
     *
     * @param o object to be checked for containment in this set
     * @return <tt>true</tt> if this set contains the specified element
     * @throws ClassCastException if the specified element cannot be
     *         compared with the elements currently in this set
     * @throws NullPointerException if the specified element is null
     */
    public boolean contains(Object o) {
        return m.containsKey(o);
    }

    /**
     * Adds the specified element to this set if it is not already present.
     * More formally, adds the specified element <tt>e</tt> to this set if
     * the set contains no element <tt>e2</tt> such that <tt>e.equals(e2)</tt>.
     * If this set already contains the element, the call leaves the set
     * unchanged and returns <tt>false</tt>.
     *
     * @param e element to be added to this set
     * @return <tt>true</tt> if this set did not already contain the
     *         specified element
     * @throws ClassCastException if <tt>e</tt> cannot be compared
     *         with the elements currently in this set
     * @throws NullPointerException if the specified element is null
     */
    public boolean add(E e) {
        return m.put(e, Boolean.TRUE) == null;
    }

    /**
     * Removes the specified element from this set if it is present.
     * More formally, removes an element <tt>e</tt> such that
     * <tt>o.equals(e)</tt>, if this set contains such an element.
     * Returns <tt>true</tt> if this set contained the element (or
     * equivalently, if this set changed as a result of the call).
     * (This set will not contain the element once the call returns.)
     *
     * @param o object to be removed from this set, if present
     * @return <tt>true</tt> if this set contained the specified element
     * @throws ClassCastException if <tt>o</tt> cannot be compared
     *         with the elements currently in this set
     * @throws NullPointerException if the specified element is null
     */
    public boolean remove(Object o) {
        return m.remove(o)!=null;
    }

    /**
     * Removes all of the elements from this set.
     */
    public void clear() {
        m.clear();
    }

    @Override
    public boolean equals(Object o){
        return m.equals(o);
    }

    @Override
    public int size() {
        return m.size();
    }

    @Override
    public Iterator<E> iterator() {
        return m.keySet().iterator();
    }

    @Override
    public String toString(){
        return m.keySet().toString();
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        super.forEach(action);
    }

    @Override
    public <T> T[] toArray(IntFunction<T[]> generator) {
        return super.toArray(generator);
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        return super.removeIf(filter);
    }

    @Override
    public Spliterator<E> spliterator() {
        return super.spliterator();
    }

    @Override
    public Stream<E> stream() {
        return super.stream();
    }

    @Override
    public Stream<E> parallelStream() {
        return super.parallelStream();
    }
}
