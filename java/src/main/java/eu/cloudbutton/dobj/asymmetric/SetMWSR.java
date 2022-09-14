package eu.cloudbutton.dobj.asymmetric;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class SetMWSR<T> implements Set<T> {

    private final Set<T> set;
    private final List<Queue<T>> queueList;
    private final ThreadLocal<Queue<T>> local;

    public SetMWSR(){
        set = new TreeSet<>();
        queueList = new CopyOnWriteArrayList<>();
        local = ThreadLocal.withInitial(() -> {
           Queue<T> queue = new QueueSASP<>();
           queueList.add(queue);
           return queue;
        });
    }

    @Override
    public int size() {
        return set.size();
    }

    @Override
    public boolean isEmpty() {
        return set.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        for (Queue queue: queueList){
            set.addAll(queue);
            queue.clear();
        }
        return set.contains(o);
    }

    @NotNull
    @Override
    public Iterator iterator() {
        return set.iterator();
    }

    @NotNull
    @Override
    public Object[] toArray() {
        return set.toArray();
    }

    @Override
    public boolean add(Object o) {
        local.get().add((T) o);
        return true;
    }

    @Override
    public boolean remove(Object o) {
        for (Queue queue: queueList){
            set.addAll(queue);
            queue.clear();
        }
        return set.remove(o);
    }

    @Override
    public boolean addAll(@NotNull Collection c) {
        for (Object o: c)
            local.get().add((T)o);

        return true;
    }

    @Override
    public void clear() {
        set.clear();
    }

    @Override
    public boolean removeAll(@NotNull Collection c) {
        set.removeAll(c);
        return true;
    }

    @Override
    public boolean retainAll(@NotNull Collection c) {
        set.retainAll(c);
        return true;
    }

    @Override
    public boolean containsAll(@NotNull Collection c) {
        return set.containsAll(c);
    }

    @NotNull
    @Override
    public Object[] toArray(@NotNull Object[] a) {
        return set.toArray(a);
    }

    @Override
    public String toString(){
        return set.toString();
    }
}
