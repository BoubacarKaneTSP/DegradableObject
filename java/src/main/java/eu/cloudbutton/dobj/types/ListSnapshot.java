package eu.cloudbutton.dobj.types;

import org.javatuples.Triplet;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ListSnapshot<T> extends AbstractList<T>{

    private final Snapshot<List<T>> snapobject;
    private final ThreadLocal<Triplet<List<T>, AtomicInteger, ArrayList<List<T>>>> tripletThreadLocal;

    public ListSnapshot() {
        snapobject = new Snapshot<>(new ConcurrentHashMap<>());
        tripletThreadLocal = new ThreadLocal<>();
    }

    protected void write(T val) { append(val); }

    @Override
    public void append(T val) {
        int name = Integer.parseInt(Thread.currentThread().getName().substring(5).replace("-thread-",""));

        if (!snapobject.obj.containsKey(name)){
            tripletThreadLocal.set(new Triplet<>(new List<>(), new AtomicInteger(), new ArrayList<>()));
            snapobject.obj.put(name, new Triplet<>(new List<>(), new AtomicInteger(), new ArrayList<>()));
        }

        java.util.List<List<T>> embedded_snap = snapobject.snap();
        tripletThreadLocal.get().getValue0().append(val);
        tripletThreadLocal.get().getValue1().incrementAndGet();

        snapobject.obj.put(name, new Triplet<>( tripletThreadLocal.get().getValue0(),
                tripletThreadLocal.get().getValue1(),
                embedded_snap));
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
