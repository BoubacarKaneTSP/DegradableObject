package eu.cloudbutton.dobj.asymmetric;

import eu.cloudbutton.dobj.incrementonly.CounterIncrementOnly;
import org.javatuples.Pair;
import org.jetbrains.annotations.NotNull;
import sun.misc.Unsafe;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This class provide a concurrent Queue that support only one poller.
 *
 * @param <E>
 * @author Boubacar Kane
 */
public class QueueSASP<E> implements Queue<E> {

    private static class Node<E> {
        volatile E item;
        volatile boolean opType;
        volatile Node<E> next;

        /**
         * Constructs a new node.  Uses relaxed write because item can
         * only be seen after publication via casNext.
         */
        Node(E item, boolean opType) {
            UNSAFE.putObject(this, itemOffset, item);
            this.opType = opType;
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

    private transient Node<E> head;
    private transient volatile Node<E> tail;
    private final CopyOnWriteArrayList<Long> listNbFor;
    private CounterIncrementOnly countNbFor;

    /**
     * Create an empty queue.
     */
    public QueueSASP() {
        tail = head = new Node<>(null, true);
        listNbFor = new CopyOnWriteArrayList<>();
        countNbFor = new CounterIncrementOnly();
    }

    /**
     * Returns an iterator over the elements in this queue in proper sequence.
     * The elements will be returned in order from first (head) to last (tail).
     *
     * <p>The returned iterator is
     * <a href="package-summary.html#Weakly"><i>weakly consistent</i></a>.
     *
     * @return an iterator over the elements in this queue in proper sequence
     */
    public Iterator<E> iterator() {
        return new Itr();
    }

    @NotNull
    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @NotNull
    @Override
    public <T> T[] toArray(@NotNull T[] a) {
        return null;
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

        /**
         * Node of the last returned item, to support remove.
         */
        private Node<E> lastRet;

        Itr() {
            restartFromHead: for (;;) {
                Node<E> h, p, q;
                for (p = h = head;; p = q) {
                    final E item;
                    if ((item = p.item) != null) {
                        nextNode = p;
                        nextItem = item;
                        break;
                    }
                    else if ((q = p.next) == null)
                        break;
                    else if (p == q)
                        continue restartFromHead;
                }
                updateHead(h, p);
                return;
            }
        }

        public boolean hasNext() {
            return nextItem != null;
        }

        public E next() {
            final Node<E> pred = nextNode;
            if (pred == null) throw new NoSuchElementException();
            // assert nextItem != null;
            lastRet = pred;
            E item = null;

            for (Node<E> p = succ(pred), q;; p = q) {
                if (p == null || (item = p.item) != null) {
                    nextNode = p;
                    E x = nextItem;
                    nextItem = item;
                    return x;
                }
                // unlink deleted nodes
                if ((q = succ(p)) != null)
                    NEXT.compareAndSet(pred, p, q);
            }
        }

        // Default implementation of forEachRemaining is "good enough".

        public void remove() {
            Node<E> l = lastRet;
            if (l == null) throw new IllegalStateException();
            // rely on a future traversal to relink.
            l.item = null;
            lastRet = null;
        }
    }

    /**
     * Returns the number of element present in the queue.
     * @return the number of element present in the queue.
     */
    @Override
    public int size() {
        int ret = 0;
        for (Node<E> p = head;;) {
            if (p.item != null) ret++;
            if (p.next==null) break;
            p = p.next;
        }
        return ret;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean contains(Object o) {
        return false;
    }

    @Override
    public boolean add(E e){
        return offer(e);
    }

    @Override
    public boolean remove(Object o) {
        final Node newNode = new Node<>(Objects.requireNonNull(o), false);

        tail.next = newNode;
        tail = newNode;

        return true;
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return false;
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends E> c) {
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

    @Override
    public void clear() {

        int size = size();

        for (int i = 0; i < size; i++) {
            poll();
        }
    }

    public Set<Pair<E, Boolean>> flush(){

        Set<Pair<E, Boolean>> eltsFlushed = new TreeSet<>();

        for (Node<E> t = tail, h = head;  h != t ; h = h.next) {
//            eltsFlushed.add(new Pair<>(h.next.item,h.next.opType));
        }

        return eltsFlushed;
    }

    /**
     * Inserts the specified element into this queue.
     * @param e
     * @return true if the element was added to this queue, else false.
     */
    @Override
    public boolean offer(E e) {

        final Node<E> newNode = new Node<>(Objects.requireNonNull(e), true);

        tail.next = newNode;
        tail = newNode;

        return true;
    }

    @Override
    public E remove(){
        return poll();
    }

    /**
     * Retrieves and removes the head of this queue, or returns null if this queue is empty.
     * @return the head of this queue, or null if this queue is empty.
     */
    @Override
    public E poll() {

        if (head != tail){
            E item = head.next.item;
            head = head.next;
            return item;
        }

        return null;

    }

    @Override
    public E element() {
        return null;
    }

    @Override
    public E peek() {
        throw new IllegalArgumentException();
    }

    @Override
    public String toString(){

        String s ="[";

        for (Node<E> node = head; node != tail; node = node.next){
            s += node.next.item;
            if (node.next != tail)
                s+=", ";
        }

        s += "]";

        return s;
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

    /**
     * Returns the successor of p, or the head node if p.next has been
     * linked to self, which will only be true if traversing with a
     * stale pointer that is now off the list.
     */
    final Node<E> succ(Node<E> p) {
        if (p == (p = p.next))
            p = head;
        return p;
    }

    // VarHandle mechanics
    private static final VarHandle HEAD;
    private static final VarHandle TAIL;
    static final VarHandle ITEM;
    static final VarHandle NEXT;
    static {
        try {
            MethodHandles.Lookup l = MethodHandles.lookup();
            HEAD = l.findVarHandle(QueueSASP.class, "head",
                    Node.class);
            TAIL = l.findVarHandle(QueueSASP.class, "tail",
                    Node.class);
            ITEM = l.findVarHandle(Node.class, "item", Object.class);
            NEXT = l.findVarHandle(Node.class, "next", Node.class);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
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
