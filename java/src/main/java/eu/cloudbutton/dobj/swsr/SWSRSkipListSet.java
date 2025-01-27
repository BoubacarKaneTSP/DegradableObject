package eu.cloudbutton.dobj.swsr;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SWSRSkipListSet<E> extends AbstractSet<E> implements Set<E> {

    private final Map m;

    public SWSRSkipListSet() {
        m = new SWSRHashMap<E,Object>();
    }

    public SWSRSkipListSet(Collection<? extends E> c) {
        m =  new SWSRSkipListMap<E, Object>();
        addAll(c);
    }

    @Override
    public int size() {
        return m.size();
    }

    @Override
    public boolean isEmpty() {
        return m.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return m.containsKey(o);
    }

    @NotNull
    @Override
    public Iterator iterator() {
        return m.keySet().iterator();
    }

    @NotNull
    @Override
    public Object[] toArray() {
        return m.keySet().toArray();
    }

    @Override
    public boolean add(Object o) {
        return m.put(o, null) == null;
    }

    @Override
    public boolean remove(Object o) {
        return m.remove(o) != null;
    }

    @Override
    public boolean addAll(@NotNull Collection c) {
        for (Object o : c)
            m.put(o, null);
        return true;
    }

    @Override
    public void clear() {
        m.clear();
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
    public boolean containsAll(@NotNull Collection c) {
        return m.keySet().containsAll(c);
    }

    @NotNull
    @Override
    public Object[] toArray(@NotNull Object[] a) {
        return m.keySet().toArray(a);
    }
}
