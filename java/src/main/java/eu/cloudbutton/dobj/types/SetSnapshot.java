package eu.cloudbutton.dobj.types;

import org.javatuples.Triplet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class SetSnapshot<T> extends AbstractSet<T>{

    private final Snapshot<Set<T>> snapobject;
    private final ThreadLocal<Triplet<Set<T>, AtomicInteger, ArrayList<Set<T>>>> tripletThreadLocal;

    public SetSnapshot() {
        snapobject = new Snapshot<>(new ConcurrentHashMap<>());
        tripletThreadLocal = new ThreadLocal<>();
    }

    public void write(T val) { add(val); }

    @Override
    public void add(T val) {
        int name = Integer.parseInt(Thread.currentThread().getName().substring(5).replace("-thread-",""));
        if (!snapobject.obj.containsKey(name)){
            tripletThreadLocal.set(new Triplet<>(new Set<>(), new AtomicInteger(), new ArrayList<>()));
            snapobject.obj.put(name, new Triplet<>(new Set<>(), new AtomicInteger(), new ArrayList<>()));
        }
        List<Set<T>> embedded_snap = snapobject.snap();
        tripletThreadLocal.get().getValue0().add(val);
        tripletThreadLocal.get().getValue1().incrementAndGet();

        snapobject.obj.put(name, new Triplet<>( tripletThreadLocal.get().getValue0(),
                tripletThreadLocal.get().getValue1(),
                embedded_snap));
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
        int name = Integer.parseInt(Thread.currentThread().getName().substring(5).replace("-thread-",""));
        if (!snapobject.obj.containsKey(name)){
            tripletThreadLocal.set(new Triplet<>(new Set<>(), new AtomicInteger(), new ArrayList<>()));
            snapobject.obj.put(name, new Triplet<>(new Set<>(), new AtomicInteger(), new ArrayList<>()));
        }
        List<Set<T>> embedded_snap = snapobject.snap();
        removed = tripletThreadLocal.get().getValue0().remove(val);
        tripletThreadLocal.get().getValue1().incrementAndGet();

        snapobject.obj.put(name, new Triplet<>( tripletThreadLocal.get().getValue0(),
                tripletThreadLocal.get().getValue1(),
                embedded_snap));

        return removed;
    }
}
