package eu.cloudbutton.dobj.snapshot;

import eu.cloudbutton.dobj.list.LinkedList;
import org.javatuples.Triplet;

import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class LinkedListSnapshot<T> extends AbstractList<T> {

    private final Snapshot<eu.cloudbutton.dobj.list.LinkedList<T>> snapobject;
    private final ThreadLocal<Triplet<eu.cloudbutton.dobj.list.LinkedList<T>, AtomicInteger, ArrayList<eu.cloudbutton.dobj.list.LinkedList<T>>>> tripletThreadLocal;
    private final ThreadLocal<Thread> name;

    public LinkedListSnapshot() {
        snapobject = new Snapshot<>(new ConcurrentHashMap<>());
        tripletThreadLocal = new ThreadLocal<>();
        name = new ThreadLocal<>();
    }

    @Override
    public Iterator<T> iterator() {
        java.util.List<eu.cloudbutton.dobj.list.LinkedList<T>> list = snapobject.snap();

        java.util.List<T> result = new ArrayList<>();

        for (eu.cloudbutton.dobj.list.LinkedList<T> ens : list) {
            result.addAll(ens.read());
        }
        return result.iterator();
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean add(T val) {
        if (name.get() == null)
            name.set(Thread.currentThread());

        if (!snapobject.obj.containsKey(name.get())){
            tripletThreadLocal.set(new Triplet<>(new eu.cloudbutton.dobj.list.LinkedList<>(), new AtomicInteger(), new ArrayList<>()));
            snapobject.obj.put(name.get(), new Triplet<>(new eu.cloudbutton.dobj.list.LinkedList<>(), new AtomicInteger(), new ArrayList<>()));
        }

        java.util.List<eu.cloudbutton.dobj.list.LinkedList<T>> embedded_snap = snapobject.snap();
        tripletThreadLocal.get().getValue0().add(val);
        tripletThreadLocal.get().getValue1().incrementAndGet();

        snapobject.obj.put(name.get(), new Triplet<>( tripletThreadLocal.get().getValue0(),
                tripletThreadLocal.get().getValue1(),
                embedded_snap));
        return true;
    }

    @Override
    public T get(int index) {
        return null;
    }

    public List<T> read() {
        java.util.List<eu.cloudbutton.dobj.list.LinkedList<T>> list = snapobject.snap();

        java.util.List<T> result = new ArrayList<>();

        for (eu.cloudbutton.dobj.list.LinkedList<T> ens : list) {
            result.addAll(ens.read());
        }
        return result;
    }

    @Override
    public boolean remove(Object val) {
        boolean removed = false;

        for ( Triplet<eu.cloudbutton.dobj.list.LinkedList<T>, AtomicInteger, java.util.List<eu.cloudbutton.dobj.list.LinkedList<T>>> triplet: snapobject.obj.values()){
            removed = triplet.getValue0().contains(val);
            if (removed)
                break;
        }

        return removed;
    }

    @Override
    public boolean contains(Object val) {
        boolean contained = false;

        for ( Triplet<eu.cloudbutton.dobj.list.LinkedList<T>, AtomicInteger, java.util.List<LinkedList<T>>> triplet: snapobject.obj.values()){
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

    /**
     * Returns a string representation of this List.
     * @return a string representation of this List.
     */
    @Override
    public String toString(){
        return "method toString not build yet";
    }
}
