package eu.cloudbutton.dobj.types;

import org.javatuples.Triplet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class SetSnapshot<T> extends AbstractSet<T>{

    private final Snapshot<Set<T>> snapobject;
    private final ThreadLocal<Triplet<Set<T>, AtomicInteger, List<Set<T>>>> tripletThreadLocal;
    private final ConcurrentMap<Thread, List<Set<T>>> embedded_snaps;

    public SetSnapshot() {
        snapobject = new Snapshot<>(new ConcurrentHashMap<>());
        tripletThreadLocal = ThreadLocal.withInitial(() -> {
            Triplet<Set<T>, AtomicInteger, List<Set<T>>> triplet = new Triplet<>(new Set<>(), new AtomicInteger(), new ArrayList<>());
            snapobject.obj.put(Thread.currentThread(), triplet);
            return triplet;
        });

        embedded_snaps = new ConcurrentHashMap<>();
        embedded_snaps.put(Thread.currentThread(), new ArrayList<>());
    }

    public void write(T val) { add(val); }

    @Override
    public void add(T val) {

        List<Set<T>> embedded_snap = snapobject.snap();
        tripletThreadLocal.get().getValue0().add(val);
        tripletThreadLocal.get().getValue1().incrementAndGet();
        embedded_snaps.put(Thread.currentThread(), embedded_snap);
    }

    public java.util.Set<T> read() {
        List<Set<T>> list = snapobject.snap();

        java.util.Set<T> result = new HashSet<>();

        for (Set<T> ens : list)
            result.addAll(ens.read());

        return result;
    }

    @Override
    public boolean contains(T val) {
        boolean contained = false;

        for ( Triplet<Set<T>, AtomicInteger, List<Set<T>>> triplet: snapobject.obj.values()){
            contained = triplet.getValue0().contains(val);
            if (contained)
                break;
        }

        return contained;
    }

    @Override
    public boolean remove(T val) {

        boolean removed;

        List<Set<T>> embedded_snap = snapobject.snap();
        removed = tripletThreadLocal.get().getValue0().remove(val);
        tripletThreadLocal.get().getValue1().incrementAndGet();
        embedded_snaps.put(Thread.currentThread(), embedded_snap);

        return removed;
    }
}
