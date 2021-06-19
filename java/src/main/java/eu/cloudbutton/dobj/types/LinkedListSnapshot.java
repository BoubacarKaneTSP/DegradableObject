package eu.cloudbutton.dobj.types;

import org.javatuples.Triplet;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class LinkedListSnapshot<T> extends AbstractList<T>{

    private final Snapshot<LinkedList<T>> snapobject;
    private final ThreadLocal<Triplet<LinkedList<T>, AtomicInteger, ArrayList<LinkedList<T>>>> tripletThreadLocal;
    private final ThreadLocal<Integer> name;

    public LinkedListSnapshot() {
        snapobject = new Snapshot<>(new ConcurrentHashMap<>());
        tripletThreadLocal = new ThreadLocal<>();
        name = new ThreadLocal<>();
    }

    @Override
    public void append(T val) {
        if (name.get() == null)
            name.set(Integer.parseInt(Thread.currentThread().getName().substring(5).replace("-thread-","")));

        if (!snapobject.obj.containsKey(name.get())){
            tripletThreadLocal.set(new Triplet<>(new LinkedList<>(), new AtomicInteger(), new ArrayList<>()));
            snapobject.obj.put(name.get(), new Triplet<>(new LinkedList<>(), new AtomicInteger(), new ArrayList<>()));
        }

        java.util.List<LinkedList<T>> embedded_snap = snapobject.snap();
        tripletThreadLocal.get().getValue0().append(val);
        tripletThreadLocal.get().getValue1().incrementAndGet();

        snapobject.obj.put(name.get(), new Triplet<>( tripletThreadLocal.get().getValue0(),
                tripletThreadLocal.get().getValue1(),
                embedded_snap));
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
