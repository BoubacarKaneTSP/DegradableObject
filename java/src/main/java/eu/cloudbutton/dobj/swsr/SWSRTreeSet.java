package eu.cloudbutton.dobj.swsr;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SWSRTreeSet<E>  extends AbstractSet<E> implements Set<E> {

    Map<E, E> m;

    public SWSRTreeSet(){
        m = new TreeMap();
    }

    @Override
    public Iterator<E> iterator() {
        return m.keySet().iterator();
    }

    @Override
    public int size() {
        return m.size();
    }

    @NotNull
    @Override
    public Object[] toArray() {
        return m.keySet().toArray();
    }

    @NotNull
    @Override
    public <T> T[] toArray(@NotNull T[] a) {
        return m.keySet().toArray(a);
    }

    @Override
    public boolean add(E e) {
        return m.put(e, null) == null;
    }

    @Override
    public boolean remove(Object o) {
        return m.remove(o) != null;
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return m.keySet().containsAll(c);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends E> c) {
        for (E o : c)
            m.put(o, null);
        return true;
    }

    @Override
    public boolean removeAll(@NotNull Collection c) {
        boolean b = false;
        for (Object o : c){
            if(m.remove(o) != null){
                b = true;
            }
        }
        return b;
    }

    @Override
    public boolean retainAll(@NotNull Collection c) {
        boolean b = false;
        for (Object o : m.keySet()){
            if (!c.contains(o)) {
                m.remove(o);
                b= true;
            }
        }
        return b;
    }

    @Override
    public void clear() {
        m.clear();
    }

    @Override
    public Spliterator<E> spliterator() {
        return Set.super.spliterator();
    }

    @Override
    public boolean isEmpty() {
        return m.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return m.containsKey(o);
    }
}
