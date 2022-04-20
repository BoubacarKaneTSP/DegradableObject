package eu.cloudbutton.dobj.Snapshot;

import eu.cloudbutton.dobj.Snapshot.SnapshotSRMW;
import org.javatuples.Pair;

import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ListSnapshotSRMW<T> extends AbstractList<T> {

    private final SnapshotSRMW<AbstractQueue<T>> snapobject;
    private final ThreadLocal<AbstractQueue<T>> listThreadLocal;

    public ListSnapshotSRMW(){
        snapobject = new SnapshotSRMW<>();
        listThreadLocal = ThreadLocal.withInitial(() -> {
            AbstractQueue<T> List = new ConcurrentLinkedQueue<>();
            snapobject.memory.put(Thread.currentThread(), new Pair<>( new Pair<>(new ConcurrentLinkedQueue<>(), 0),
                    new Pair<>(new ConcurrentLinkedQueue<>(), 0)
            ));
            return List;
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

        listThreadLocal.get().add(val);
        snapobject.update(listThreadLocal.get());

        return true;
    }

    @Override
    public T get(int index) {
        return null;
    }

    public List<T> read() {

        List<T> result = new ArrayList<>();

        for (AbstractQueue<T> l : snapobject.snap())
            result.addAll(l);

        return result;
    }

    @Override
    public boolean remove(Object val) {

        boolean removed;
        removed = listThreadLocal.get().remove(val);
        snapobject.update(listThreadLocal.get());

        return removed;
    }

    @Override
    public boolean contains(Object val) {

        boolean contained = false;

        for (AbstractQueue<T> list : snapobject.snap()){
            contained = list.contains(val);
            if (contained)
                break;
        }
        return contained;
    }

    @Override
    public void clear() {
        throw new java.lang.Error("Remove not build yet");
    }

}
