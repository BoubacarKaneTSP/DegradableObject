package eu.cloudbutton.dobj.types;

import org.javatuples.Triplet;

import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ListSnapshot<T> extends AbstractQueue<T> implements Queue<T> {

    private final Snapshot<AbstractQueue<T>> snapobject;
    private final ThreadLocal<Triplet<AbstractQueue<T>, AtomicInteger, java.util.List<AbstractQueue<T>>>> tripletThreadLocal;
    private final ConcurrentMap<Thread, java.util.List<AbstractQueue<T>>> embedded_snaps;

    public ListSnapshot() {
        snapobject = new Snapshot<>(new ConcurrentHashMap<>());
        tripletThreadLocal = ThreadLocal.withInitial(() -> {
            Triplet<AbstractQueue<T>, AtomicInteger, java.util.List<AbstractQueue<T>>> triplet = new Triplet<>(new ConcurrentLinkedQueue<>(), new AtomicInteger(), new ArrayList<>());
            snapobject.obj.put(Thread.currentThread(), triplet);
            return triplet;
        });

        embedded_snaps = new ConcurrentHashMap<>();
        embedded_snaps.put(Thread.currentThread(), new ArrayList<>());
    }

    @Override
    public Iterator<T> iterator() {
        return null;
    }

    @Override
    public int size() {
        return 0;
    }

    protected void write(T val) { add(val); }

    @Override
    public boolean add(T val) {

        java.util.List<AbstractQueue<T>> embedded_snap = snapobject.snap();
        tripletThreadLocal.get().getValue0().add(val);
        tripletThreadLocal.get().getValue1().incrementAndGet();
        embedded_snaps.put(Thread.currentThread(), embedded_snap);
        return true;
    }

    public java.util.List<T> read() {
        java.util.List<AbstractQueue<T>> list = snapobject.snap();

        java.util.List<T> result = new ArrayList<>();

        for (AbstractQueue<T> ens : list) {
            result.addAll(ens);
        }
        return result;
    }

    @Override
    public boolean remove(Object val) {
        throw new java.lang.Error("Remove not build yet");
    }

    @Override
    public boolean contains(Object val) {
        boolean contained = false;

        for ( Triplet<AbstractQueue<T>, AtomicInteger, java.util.List<AbstractQueue<T>>> triplet: snapobject.obj.values()){

            contained = triplet.getValue0().contains(val);

            if (contained)
                break;

        }

        return contained;
    }

    @Override
    public void clear() {
        throw new java.lang.Error("Remove not build yet");
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

    @Override
    public String toString(){
        return "method toString not build yet";
    }
}
