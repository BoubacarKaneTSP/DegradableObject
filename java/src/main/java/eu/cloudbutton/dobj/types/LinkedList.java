package eu.cloudbutton.dobj.types;

import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class LinkedList <T> extends AbstractQueue<T> implements Queue<T> {
    AtomicReference<Node<T>> head; // head of list

    public LinkedList() {
        this.head = null;
    }

    @Override
    public Iterator<T> iterator() {
        List<T> result = new ArrayList();

        for(AtomicReference<Node<T>> cur = this.head ; cur != null ; cur = cur.get().next){
            result.add(cur.get().data);
        }
        return result.iterator();
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean add(T data){

        this.head = new AtomicReference<>(new Node<T>(data, head));
        return true;
    }

    public List<T> read() {
        List<T> result = new ArrayList();

        for(AtomicReference<Node<T>> cur = this.head ; cur != null ; cur = cur.get().next){
            result.add(cur.get().data);
        }
        return result;
    }

    @Override
    public boolean contains(Object val) {
        for(AtomicReference<Node<T>> cur = this.head ; cur != null ; cur = cur.get().next){
            if (cur.get().data == val)
                return true;
        }
        return false;
    }

    @Override
    public boolean remove(Object data){
        if (this.head != null){
            if (this.head.get().data == data)
                this.head = this.head.get().next;
            else{
                for(AtomicReference<Node<T>> pred = this.head ; pred.get().next != null ; pred = pred.get().next){
                    if (pred.get().next.get().data == data){
                        pred.get().next = pred.get().next.get().next;
                        return true;
                    }
                }
            }
        }
        return false;
    }

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

    @Override
    public boolean offer(T t) {
        return false;
    }

    @Override
    public T poll() {
        return null;
    }

    @Override
    public T peek() {
        return null;
    }

    public static class Node <T>{

        T data;
        AtomicReference<Node<T>> next;

        public Node(T data, AtomicReference<Node<T>> next){
            this.data = data;
            this.next = next;
        }
    }

    @Override
    public String toString(){
        return "method toString not build yet";
    }
}
