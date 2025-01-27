package eu.cloudbutton.dobj.list;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This class provide a LinkedList.
 * WIP.
 *
 * @author Boubacar Kane
 * */
public class LinkedList <T> implements List<T> {
    AtomicReference<Node<T>> head; // head of list

    /**
     * Create an empty LinkedList.
     */
    public LinkedList() {
        this.head = null;
    }

    /**
     * Returns an iterator over the elements in this list in proper sequence.
     * @return an iterator over the elements in this list in proper sequence.
     */
    @Override
    public Iterator<T> iterator() {
        List<T> result = new ArrayList();

        for(AtomicReference<Node<T>> cur = this.head ; cur != null ; cur = cur.get().next){
            result.add(cur.get().data);
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
    public boolean add(T element){

        this.head = new AtomicReference<>(new Node<>(element, head));
        return true;
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
        List<T> result = new ArrayList();

        for(AtomicReference<Node<T>> cur = this.head ; cur != null ; cur = cur.get().next){
            result.add(cur.get().data);
        }
        return result;
    }

    /**
     * Returns true if this List contains the specified element.
     * @param o
     * @return true if this List contains the specified element.
     */
    @Override
    public boolean contains(Object o) {
        for(AtomicReference<Node<T>> cur = this.head ; cur != null ; cur = cur.get().next){
            if (cur.get().data == o)
                return true;
        }
        return false;
    }

    /**
     * Removes a single instance of the specified element from this List.
     * Each process can only delete an element that it has previously added.
     * @param o
     * @return true if the element has been removed.
     */
    @Override
    public boolean remove(Object o){
        if (this.head != null){
            if (this.head.get().data == o)
                this.head = this.head.get().next;
            else{
                for(AtomicReference<Node<T>> pred = this.head ; pred.get().next != null ; pred = pred.get().next){
                    if (pred.get().next.get().data == o){
                        pred.get().next = pred.get().next.get().next;
                        return true;
                    }
                }
            }
        }
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
     * Removes all of the elements from this List.
     */
    public void clear(){

        AtomicReference<Node<T>> cur;
        AtomicReference<Node<T>> tmp;

        if (head != null){
            cur = head.get().next;
            head = null;

            while(cur != null){
                tmp = cur;
                cur = cur.get().next;
                tmp.set(null);
            }
        }
    }

    public static class Node <T>{

        T data;
        AtomicReference<Node<T>> next;

        public Node(T data, AtomicReference<Node<T>> next){
            this.data = data;
            this.next = next;
        }
    }

    /**
     * Returns a string representation of this List.
     * @return a string representation of this List.
     */
    @Override
    public String toString(){
        return "method toString not build yet";
    }
}
