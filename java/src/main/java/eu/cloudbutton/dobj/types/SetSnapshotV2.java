package eu.cloudbutton.dobj.types;

import org.javatuples.Pair;

import java.util.HashSet;

public class SetSnapshotV2<T> extends AbstractSet<T>{

    private final SnapshotV2<eu.cloudbutton.dobj.types.Set<T>> snapobject;
    private final ThreadLocal<eu.cloudbutton.dobj.types.Set<T>> setThreadLocal;

    public SetSnapshotV2(){
        snapobject = new SnapshotV2<>();
        setThreadLocal = new ThreadLocal<>();
    }

    @Override
    public void add(T val) {
        int name = Integer.parseInt(Thread.currentThread().getName().substring(5).replace("-thread-",""));
        if(!snapobject.memory.containsKey(name)){
            setThreadLocal.set(new eu.cloudbutton.dobj.types.Set<>());
            snapobject.memory.put   (   name,
                                        new Pair<>( new Pair<>(new eu.cloudbutton.dobj.types.Set<>(), 0),
                                                    new Pair<>(new eu.cloudbutton.dobj.types.Set<>(), 0)
                                        )
                                    );
        }
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
        int name = Integer.parseInt(Thread.currentThread().getName().substring(5).replace("-thread-",""));
        if(!snapobject.memory.containsKey(name)){
            setThreadLocal.set(new eu.cloudbutton.dobj.types.Set<>());
            snapobject.memory.put   (   name,
                    new Pair<>( new Pair<>(new eu.cloudbutton.dobj.types.Set<>(), 0),
                            new Pair<>(new eu.cloudbutton.dobj.types.Set<>(), 0)
                    )
            );
        }

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
