package eu.cloudbutton.dobj.types;

import org.javatuples.Pair;

import java.util.HashSet;

public class SetSnapshotSRMW<T> extends AbstractSet<T>{

    private final SnapshotSRMW<Set<T>> snapobject;
    private final ThreadLocal<eu.cloudbutton.dobj.types.Set<T>> setThreadLocal;

    public SetSnapshotSRMW(){
        snapobject = new SnapshotSRMW<>();
        setThreadLocal = ThreadLocal.withInitial(() -> {
            Set<T> set = new Set<>();
            snapobject.memory.put(Thread.currentThread(), new Pair<>( new Pair<>(new Set<>(), 0),
                    new Pair<>(new Set<>(), 0)
            ));
            return set;
        });
    }

    @Override
    public void add(T val) {
        setThreadLocal.get().add(val);
        snapobject.update(setThreadLocal.get());
    }

    @Override
    public java.util.Set<T> read() {

        java.util.Set<T> result = new HashSet<>();

        for ( Set<T> set :snapobject.snap()){
            result.addAll(set.read());
        }
        return result;
    }

    @Override
    public boolean remove(T val) {

        boolean removed;

        removed = setThreadLocal.get().remove(val);
        snapobject.update(setThreadLocal.get());

        return removed;
    }

    @Override
    public boolean contains(T val) {

        boolean contained = false;

        for ( Set<T> set :snapobject.snap()){
            contained = set.contains(val);
            if (contained)
                break;
        }

        return contained;
    }
}
