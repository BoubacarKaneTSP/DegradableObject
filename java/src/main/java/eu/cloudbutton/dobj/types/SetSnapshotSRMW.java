package eu.cloudbutton.dobj.types;

import org.javatuples.Pair;

import java.util.AbstractSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.ConcurrentSkipListSet;

public class SetSnapshotSRMW<T> extends AbstractSet<T> {

    private final SnapshotSRMW<AbstractSet<T>> snapobject;
    private final ThreadLocal<AbstractSet<T>> setThreadLocal;

    public SetSnapshotSRMW(){
        snapobject = new SnapshotSRMW<>();
        setThreadLocal = ThreadLocal.withInitial(() -> {
            AbstractSet<T> set = new ConcurrentSkipListSet<>();
            snapobject.memory.put(Thread.currentThread(), new Pair<>( new Pair<>(new ConcurrentSkipListSet<>(), 0),
                    new Pair<>(new ConcurrentSkipListSet<>(), 0)
            ));
            return set;
        });
    }

    @Override
    public Iterator<T> iterator() {
        return null;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean add(T val) {
        setThreadLocal.get().add(val);
        snapobject.update(setThreadLocal.get());
        return false;
    }

    public java.util.Set<T> read() {

        java.util.Set<T> result = new HashSet<>();

        for ( AbstractSet<T> set :snapobject.snap()){
            result.addAll(set);
        }
        return result;
    }

    @Override
    public boolean remove(Object val) {

        boolean removed;

        removed = setThreadLocal.get().remove(val);
        snapobject.update(setThreadLocal.get());

        return removed;
    }

    @Override
    public boolean contains(Object val) {

        boolean contained = false;

        for ( AbstractSet<T> set :snapobject.snap()){
            contained = set.contains(val);
            if (contained)
                break;
        }

        return contained;
    }

    @Override
    public String toString(){
        return "method toString not build yet";
    }
}
