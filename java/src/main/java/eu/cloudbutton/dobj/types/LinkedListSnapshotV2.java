package eu.cloudbutton.dobj.types;

import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.List;

public class LinkedListSnapshotV2<T> extends AbstractList<T>{

    private final SnapshotV2<LinkedList<T>> snapobject;
    private final ThreadLocal<LinkedList<T>> listThreadLocal;

    public LinkedListSnapshotV2(){
        snapobject = new SnapshotV2<>();
        listThreadLocal = new ThreadLocal<>();
    }

    @Override
    public void append(T val) {
        int name = Integer.parseInt(Thread.currentThread().getName().substring(5).replace("-thread-",""));
        if(!snapobject.memory.containsKey(name)){
            listThreadLocal.set(new LinkedList<>());
            snapobject.memory.put   (   name,
                    new Pair<>( new Pair<>(new LinkedList<>(), 0),
                            new Pair<>(new LinkedList<>(), 0)
                    )
            );
        }
        listThreadLocal.get().append(val);
        snapobject.update(listThreadLocal.get());
    }

    @Override
    public List<T> read() {

        List<T> result = new ArrayList<>();

        for (LinkedList<T> l : snapobject.snap())
            result.addAll(l.read());

        return result;
    }

    @Override
    public boolean remove(T val) {

        int name = Integer.parseInt(Thread.currentThread().getName().substring(5).replace("-thread-",""));
        if(!snapobject.memory.containsKey(name)){
            listThreadLocal.set(new LinkedList<>());
            snapobject.memory.put   (   name,
                    new Pair<>( new Pair<>(new LinkedList<>(), 0),
                            new Pair<>(new LinkedList<>(), 0)
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

        for (LinkedList<T> list : snapobject.snap()){
            contained = list.contains(val);
            if (contained)
                break;
        }
        return contained;
    }
}
