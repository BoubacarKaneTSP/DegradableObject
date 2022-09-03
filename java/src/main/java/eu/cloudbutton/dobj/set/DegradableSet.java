package eu.cloudbutton.dobj.set;

import eu.cloudbutton.dobj.map.DegradableMap;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.Set;

public class DegradableSet<T> implements Set<T> {

    private final DegradableMap<T, Object> set;

    public DegradableSet() {
        this.set = new DegradableMap<>();
    }


    @NotNull
    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @NotNull
    @Override
    public <T1> T1[] toArray(@NotNull T1[] a) {
        return null;
    }

    @Override
    public int size() {

        return set.size();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean add(T val) {
        return set.put(val,0) == null;
    }

    @Override
    public boolean remove(Object val) {
        return set.remove(val) != null;
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return false;
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends T> c) {
        return false;
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        return false;
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        return false;
    }

    @Override
    public void clear() {

    }

    @Override
    public boolean contains(Object val) {
        return set.containsKey(val);
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return set.iterator();
    }

    @Override
    public String toString(){
        return set.keySet().toString();
    }

}
