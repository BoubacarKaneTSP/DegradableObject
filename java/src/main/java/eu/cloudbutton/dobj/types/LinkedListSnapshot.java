package eu.cloudbutton.dobj.types;

import org.javatuples.Triplet;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class LinkedListSnapshot<T> extends AbstractList<T>{

    private final Snapshot<LinkedList<T>> snapobject;
    private final ThreadLocal<Triplet<LinkedList<T>, AtomicInteger, List<LinkedList<T>>>> tripletThreadLocal;
    private final ConcurrentMap<Thread, List<LinkedList<T>>> embedded_snaps;

    public LinkedListSnapshot() {
        snapobject = new Snapshot<>(new ConcurrentHashMap<>());
        tripletThreadLocal = ThreadLocal.withInitial(() -> {
            Triplet<LinkedList<T>, AtomicInteger, List<LinkedList<T>>> triplet = new Triplet<>(new LinkedList<>(), new AtomicInteger(), new ArrayList<>());
            snapobject.obj.put(Thread.currentThread(), triplet);
            return triplet;
        });
        embedded_snaps = new ConcurrentHashMap<>();
        embedded_snaps.put(Thread.currentThread(), new ArrayList<>());    }

    @Override
    public void append(T val) {
        List<LinkedList<T>> embedded_snap = snapobject.snap();
        tripletThreadLocal.get().getValue0().append(val);
        tripletThreadLocal.get().getValue1().incrementAndGet();
        embedded_snaps.put(Thread.currentThread(), embedded_snap);
    }

    @Override
    public List<T> read() {
        java.util.List<LinkedList<T>> list = snapobject.snap();

        java.util.List<T> result = new ArrayList<>();

        for (LinkedList<T> ens : list) {
            result.addAll(ens.read());
        }
        return result;
    }

    @Override
    public boolean remove(T val) {
        boolean removed = false;

        for ( Triplet<LinkedList<T>, AtomicInteger, java.util.List<LinkedList<T>>> triplet: snapobject.obj.values()){
            removed = triplet.getValue0().contains(val);
            if (removed)
                break;
        }

        return removed;
    }

    @Override
    public boolean contains(T val) {
        boolean contained = false;

        for ( Triplet<LinkedList<T>, AtomicInteger, java.util.List<LinkedList<T>>> triplet: snapobject.obj.values()){
            contained = triplet.getValue0().contains(val);
            if (contained)
                break;
        }

        return contained;
    }
}
