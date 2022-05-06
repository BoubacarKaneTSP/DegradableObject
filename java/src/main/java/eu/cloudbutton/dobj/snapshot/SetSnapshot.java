package eu.cloudbutton.dobj.snapshot;

import org.javatuples.Triplet;

import java.util.*;
import java.util.AbstractSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

public class SetSnapshot<T> extends AbstractSet<T> {

    private final Snapshot<AbstractSet<T>> snapobject;
    private final ThreadLocal<Triplet<AbstractSet<T>, AtomicInteger, List<AbstractSet<T>>>> tripletThreadLocal;
    private final ConcurrentMap<Thread, List<AbstractSet<T>>> embedded_snaps;

    public SetSnapshot() {
        snapobject = new Snapshot<>(new ConcurrentHashMap<>());
        tripletThreadLocal = ThreadLocal.withInitial(() -> {
            Triplet<AbstractSet<T>, AtomicInteger, List<AbstractSet<T>>> triplet = new Triplet<>(new ConcurrentSkipListSet<>(), new AtomicInteger(), new ArrayList<>());
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

    public void write(T val) { add(val); }

    @Override
    public boolean add(T val) {
        boolean b;

        List<AbstractSet<T>> embedded_snap = snapobject.snap();
        b = tripletThreadLocal.get().getValue0().add(val);
        tripletThreadLocal.get().getValue1().incrementAndGet();
        embedded_snaps.put(Thread.currentThread(), embedded_snap);
        return false;
    }

    public java.util.Set<T> read() {
        List<AbstractSet<T>> list = snapobject.snap();

        java.util.Set<T> result = new HashSet<>();

        for (AbstractSet<T> ens : list)
            result.addAll(ens);

        return result;
    }

    @Override
    public boolean contains(Object val) {
        boolean contained = false;

        for ( Triplet<AbstractSet<T>, AtomicInteger, List<AbstractSet<T>>> triplet: snapobject.obj.values()){
            contained = triplet.getValue0().contains(val);
            if (contained)
                break;
        }

        return contained;
    }

    @Override
    public boolean remove(Object val) {

        boolean removed;

        List<AbstractSet<T>> embedded_snap = snapobject.snap();
        removed = tripletThreadLocal.get().getValue0().remove(val);
        tripletThreadLocal.get().getValue1().incrementAndGet();
        embedded_snaps.put(Thread.currentThread(), embedded_snap);

        return removed;
    }

    @Override
    public String toString(){
        return "method toString not build yet";
    }
}
