package eu.cloudbutton.dobj.types;

import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.List;

public class ListSnapshotV2<T> extends AbstractList<T>{

    private final SnapshotV2<eu.cloudbutton.dobj.types.List<T>> snapobject;
    private final ThreadLocal<eu.cloudbutton.dobj.types.List<T>> listThreadLocal;
    private final ThreadLocal<Integer> name;

    public ListSnapshotV2(){
        snapobject = new SnapshotV2<>();
        listThreadLocal = new ThreadLocal<>();
        name = new ThreadLocal<>();
    }

    @Override
    public void append(T val) {
        if (name.get() == null)
            name.set(Integer.parseInt(Thread.currentThread().getName().substring(5).replace("-thread-","")));
        if(!snapobject.memory.containsKey(name.get())){
            listThreadLocal.set(new eu.cloudbutton.dobj.types.List<>());
            snapobject.memory.put   (   name.get(),
                    new Pair<>( new Pair<>(new eu.cloudbutton.dobj.types.List<>(), 0),
                            new Pair<>(new eu.cloudbutton.dobj.types.List<>(), 0)
                    )
            );
        }
        listThreadLocal.get().append(val);
        snapobject.update(listThreadLocal.get());
    }

    @Override
    public List<T> read() {

        List<T> result = new ArrayList<>();

        for (eu.cloudbutton.dobj.types.List<T> l : snapobject.snap())
            result.addAll(l.read());

        return result;
    }

    @Override
    public boolean remove(T val) {

        if (name.get() == null)
            name.set(Integer.parseInt(Thread.currentThread().getName().substring(5).replace("-thread-","")));

        if(!snapobject.memory.containsKey(name.get())){
            listThreadLocal.set(new eu.cloudbutton.dobj.types.List<>());
            snapobject.memory.put   (   name.get(),
                    new Pair<>( new Pair<>(new eu.cloudbutton.dobj.types.List<>(), 0),
                            new Pair<>(new eu.cloudbutton.dobj.types.List<>(), 0)
                    )
            );
        }

        boolean removed;
        removed = listThreadLocal.get().remove(val);
        snapobject.update(listThreadLocal.get());

        return removed;
    }

    @Override
    public boolean contains(T val) {

        boolean contained = false;

        for (eu.cloudbutton.dobj.types.List<T> list : snapobject.snap()){
            contained = list.contains(val);
            if (contained)
                break;
        }
        return contained;
    }
}
