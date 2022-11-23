package eu.cloudbutton.dobj.asymmetric;

import org.javatuples.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class SetMWSR<T> implements Set<T> {

    private final Set<T> set;
    private final List<QueueSASP<T>> queueList;
    private final ThreadLocal<QueueSASP<T>> local;

    public SetMWSR(){
        set = new TreeSet<>();
        queueList = new CopyOnWriteArrayList<>();
        local = ThreadLocal.withInitial(() -> {
           QueueSASP<T> queue = new QueueSASP<>();
           queueList.add(queue);
           return queue;
        });
    }

    @Override
    public int size() {
        update();
        return set.size();
    }

    @Override
    public boolean isEmpty() {
        return set.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        update();
        return set.contains(o);
    }

    @NotNull
    @Override
    public Iterator iterator() {
        update();
        return set.iterator();
    }

    @NotNull
    @Override
    public Object[] toArray() {
        return set.toArray();
    }

    @Override
    public boolean add(Object o) {
        return local.get().offer((T) o);
    }

    @Override
    public boolean remove(Object o) {return local.get().remove(o);}

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

    private void update(){
        for (QueueSASP<T> queue: queueList){
//            System.out.println("Queue => " + queue);
            List<Pair<T, Boolean>> eltsFlushed = queue.flush();
//            System.out.println("eltsFlushed => " + eltsFlushed);

            for (Pair<T, Boolean> element: eltsFlushed){

                if (element.getValue1()) {
                    try{
                        set.add(element.getValue0());
                    }catch (NullPointerException e){
                        System.out.println(e);
                        System.out.println(element.getValue0());
                        System.exit(1);
                    }
                }
                else
                    set.remove(element.getValue0());
            }
        }
    }
}
