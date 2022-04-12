package eu.cloudbutton.dobj.types;

import org.javatuples.Pair;

import java.util.*;
import java.util.List;
import java.util.Queue;

public class LinkedListSnapshotSRMW<T> extends AbstractList<T> {

    private final SnapshotSRMW<LinkedList<T>> snapobject;
    private final ThreadLocal<LinkedList<T>> listThreadLocal;

    public LinkedListSnapshotSRMW(){
        snapobject = new SnapshotSRMW<>();
        listThreadLocal = ThreadLocal.withInitial(() -> {
            LinkedList<T> linkedList = new LinkedList<>();
            snapobject.memory.put(Thread.currentThread(), new Pair<>( new Pair<>(new LinkedList<>(), 0),
                    new Pair<>(new LinkedList<>(), 0)
            ));
            return linkedList;
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

        for (LinkedList<T> l : snapobject.snap())
            result.addAll(l.read());

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

        for (LinkedList<T> list : snapobject.snap()){
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
