package eu.cloudbutton.dobj.types;

import org.javatuples.Pair;

import java.util.HashSet;

public class SetSnapshotV2<T> extends AbstractSet<T>{

    private final SnapshotSRMW<Set<T>> snapobject;
    private final ThreadLocal<eu.cloudbutton.dobj.types.Set<T>> setThreadLocal;
    private final ThreadLocal<Integer> name;

    public SetSnapshotV2(){
        snapobject = new SnapshotSRMW<>();
        setThreadLocal = new ThreadLocal<>();
        name = new ThreadLocal<>();
    }

    @Override
    public void add(T val) {
        if (name.get() == null)
            name.set(Integer.parseInt(Thread.currentThread().getName().substring(5).replace("-thread-","")));
        if(!snapobject.memory.containsKey(name.get())){
            setThreadLocal.set(new eu.cloudbutton.dobj.types.Set<>());
            snapobject.memory.put   (   name.get(),
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
        if (name.get() == null)
            name.set(Integer.parseInt(Thread.currentThread().getName().substring(5).replace("-thread-","")));

        if(!snapobject.memory.containsKey(name.get())){
            setThreadLocal.set(new eu.cloudbutton.dobj.types.Set<>());
            snapobject.memory.put   (   name.get(),
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
