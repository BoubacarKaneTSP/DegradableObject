package eu.cloudbutton.dobj.types;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;

public class AddOnlyQueue<E> extends AbstractQueue<E> {


    private static class Node<E> {
        volatile E item;
        volatile Node<E> next;
        volatile Node<E> pred;

        /**
         * Constructs a new node.  Uses relaxed write because item can
         * only be seen after publication via casNext.
         */
        Node(E item) {
            UNSAFE.putObject(this, itemOffset, item);
        }

        boolean casItem(E cmp, E val) {
            return UNSAFE.compareAndSwapObject(this, itemOffset, cmp, val);
        }

        void lazySetNext(Node<E> val) {
            UNSAFE.putOrderedObject(this, nextOffset, val);
        }

        boolean casNext(Node<E> cmp, Node<E> val) {
            return UNSAFE.compareAndSwapObject(this, nextOffset, cmp, val);
        }

        // Unsafe mechanics

        private static final sun.misc.Unsafe UNSAFE;
        private static final long itemOffset;
        private static final long nextOffset;

        static {
            try {
                Field f = Unsafe.class.getDeclaredField("theUnsafe");
                f.setAccessible(true);
                UNSAFE = (Unsafe) f.get(null);
                Class<?> k = Node.class;
                itemOffset = UNSAFE.objectFieldOffset
                        (k.getDeclaredField("item"));
                nextOffset = UNSAFE.objectFieldOffset
                        (k.getDeclaredField("next"));
            } catch (Exception e) {
                throw new Error(e);
            }
        }
    }


    private transient volatile Node<E> head;

    private transient volatile Node<E> tail;

    public AddOnlyQueue() {
        tail = head = new Node<>(null);
    }

    @Override
    public Iterator<E> iterator() {
        return new Itr();
    }

    private class Itr implements Iterator<E> {
        /**
         * Next node to return item for.
         */
        private Node<E> nextNode;

        /**
         * nextItem holds on to item fields because once we claim
         * that an element exists in hasNext(), we must return it in
         * the following next() call even if it was in the process of
         * being removed when hasNext() was called.
         */
        private E nextItem;

        Itr() {
            nextItem = head.item;
            nextNode = head;
        }

        public boolean hasNext() {
            return nextItem != null;
        }

        public E next() {
            final Node<E> pred = nextNode;
            if (pred == null) throw new NoSuchElementException();
            // assert nextItem != null;
            E item;

            for (Node<E> p = pred.pred; ;) {
                if ((item = p.item) != null || p.pred != null) {
                    nextNode = p;
                    E x = nextItem;
                    nextItem = item;
                    return x;
                }else if(p.item == null && p.pred == null){
                    nextNode = null;
                    E x = nextItem;
                    nextItem = null;
                    return x;
                }
            }
        }
    }

    private Node<E> newNode(E e) {
        final Node<E> newNode = new Node<>(e);
        return newNode;
    }

    @Override
    public int size() {
        int ret = 0;
        for (Node<E> p = tail;;) {
            if (p.item != null) ret++;
            if (p.next==null) break;
            p = p.next;
        }
        return ret;
    }

    @Override
    public boolean offer(E e) {
        final Node<E> newNode = newNode(Objects.requireNonNull(e));

        for (;;) {
            Node<E> last = head, next = last.next;
            if (last == head) {
                // p is last node
                if (next == null) {
                    newNode.pred = last;
                    if (last.casNext(last.next, newNode)) {
                        casHead(last, newNode);
                        return true;
                    }
                } else{
                    casHead(last, next);
                }
            }
        }
    }

    public boolean add(E e){
        return offer(e);
    }

    @Override
    public E poll() {
        /*
        restartFromHead:
        for (;;) {
            for (Node<E> h = head, p = h, q;;) {
                E item = p.item;

                if (item != null && p.casItem(item, null)) {
                    // Successful CAS is the linearization point
                    // for item to be removed from this queue.
//					if (p != h) // hop two nodes at a time
//						updateHead(h, ((q = p.next) != null) ? q : p);
                    if (p != h && p.next != null)
                        head.lazySetNext(p.next);
                    return item;
                }
                else if ((q = p.next) == null) {
                    updateHead(h, p);
                    return null;
                }
                else if (p == q)
                    continue restartFromHead;
                else
                    p = q;
            }



        }*/

        return null;
    }

    @Override
    public E peek() {
        throw new IllegalArgumentException();
    }

    /**
     * Tries to CAS head to p. If successful, repoint old head to itself
     * as sentinel for succ(), below.
     */
    final void updateHead(Node<E> h, Node<E> p) {
        if (h != p && casHead(h, p))
            h.lazySetNext(h);
    }


    private boolean casHead(Node<E> cmp, Node<E> val) {
        return UNSAFE.compareAndSwapObject(this, headOffset, cmp, val);
    }

    private boolean casTail(Node<E> cmp, Node<E> val) {
        return UNSAFE.compareAndSwapObject(this, tailOffset, cmp, val);
    }

    private void lazySetTail(Node<E> val) {
        UNSAFE.putOrderedObject(this, tailOffset, val);
    }


    private static void checkNotNull(Object v) {
        if (v == null)
            throw new NullPointerException();
    }

    // Unsafe mechanics

    private static final sun.misc.Unsafe UNSAFE;
    private static final long headOffset;
    private static final long tailOffset;
    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            UNSAFE = (Unsafe) f.get(null);
            Class<?> k = ConcurrentLinkedQueue.class;
            headOffset = UNSAFE.objectFieldOffset
                    (k.getDeclaredField("head"));
            tailOffset = UNSAFE.objectFieldOffset
                    (k.getDeclaredField("tail"));
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }
}
