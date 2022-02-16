package eu.cloudbutton.dobj.types;

import java.util.List;

public abstract class AbstractList<E> {

    /**
     * Appends the specified element to this list.
     * @param element element to be appended to this list
     */
    public abstract void append(E element);

    /**
     * Returns a java.util.List that contains all elements
     * @return all elements stored in the object.
     */
    public abstract List<E> read();

    /**
     * Each process can only delete an element that it has previously added.
     * It removes only one occurrence.
     * @param element element to remove.
     * @return true if the element has been removed.
     */
    public abstract boolean remove(E element);

    /**
     * Returns true if this List contains the specified element.
     * @param element
     * @return true if this List contains the specified element.
     */
    public abstract boolean contains(E element);

    /**
     * Delete all elements in the List.
     */
    public abstract void clear();
}
