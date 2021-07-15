package eu.cloudbutton.dobj.types;

import org.javatuples.Triplet;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ListSnapshot<T> extends AbstractList<T>{

    private final Snapshot<List<T>> snapobject;
    private final ThreadLocal<Triplet<List<T>, AtomicInteger, java.util.List<List<T>>>> tripletThreadLocal;
    private final ConcurrentMap<Thread, java.util.List<List<T>>> embedded_snaps;

    public ListSnapshot() {
        snapobject = new Snapshot<>(new ConcurrentHashMap<>());
        tripletThreadLocal = ThreadLocal.withInitial(() -> {
            Triplet<List<T>, AtomicInteger, java.util.List<List<T>>> triplet = new Triplet<>(new List<>(), new AtomicInteger(), new ArrayList<>());
            snapobject.obj.put(Thread.currentThread(), triplet);
            return triplet;
        });

        embedded_snaps = new ConcurrentHashMap<>();
        embedded_snaps.put(Thread.currentThread(), new ArrayList<>());
    }

    protected void write(T val) { append(val); }

    @Override
    public void append(T val) {

        java.util.List<List<T>> embedded_snap = snapobject.snap();
        tripletThreadLocal.get().getValue0().append(val);
        tripletThreadLocal.get().getValue1().incrementAndGet();
        embedded_snaps.put(Thread.currentThread(), embedded_snap);
    }

    @Override
    public java.util.List<T> read() {
        java.util.List<List<T>> list = snapobject.snap();

        java.util.List<T> result = new ArrayList<>();

        for (List<T> ens : list) {
            result.addAll(ens.read());
        }
        return result;
    }

    @Override
    public boolean remove(T val) {
        throw new java.lang.Error("Remove not build yet");
    }

    @Override
    public boolean contains(T val) {
        boolean contained = false;

        for ( Triplet<List<T>, AtomicInteger, java.util.List<List<T>>> triplet: snapobject.obj.values()){

            contained = triplet.getValue0().contains(val);

            if (contained)
                break;

        }

        return contained;
    }
}
