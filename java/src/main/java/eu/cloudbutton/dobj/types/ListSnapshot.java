package eu.cloudbutton.dobj.types;

import org.javatuples.Triplet;

import java.util.ArrayList;
import java.util.Collection;
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
        String name = Thread.currentThread().getName();

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

        for (List<T> count : list) {
            result.addAll((Collection<? extends T>) count);
        }
        return result;
    }

    @Override
    public void remove(T val) {
        throw new java.lang.Error("Remove not build yet");
    }
}
