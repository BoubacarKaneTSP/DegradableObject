package eu.cloudbutton.dobj.set;

import eu.cloudbutton.dobj.javaobj.ConcHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentHashSet<E> extends AbstractSet<E> implements Set<E> {

    Map m;

    public ConcurrentHashSet(){
        m = new ConcHashMap();
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
    public Iterator<E> iterator() {
        return m.keySet().iterator();
    }

    @NotNull
    @Override
    public Object[] toArray() {
        return m.keySet().toArray();
    }

    @NotNull
    @Override
    public <T> T[] toArray(@NotNull T[] a) {
        return (T[]) m.keySet().toArray(a);
    }

    @Override
    public boolean add(E e) {
        return m.put(e, 0) == null;
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
        return true;     }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
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
    public boolean removeAll(@NotNull Collection<?> c) {
        boolean b = false;
        for (Object o : c){
            if(m.remove(o) != null){
                b = true;
            }
        }
        return b;
    }

    @Override
    public void clear() {
        m.clear();
    }

    public int getNbBin(){
        return ((ConcHashMap)m).getNbBin();
    }
}
