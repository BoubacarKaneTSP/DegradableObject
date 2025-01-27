package eu.cloudbutton.dobj.set;

import org.jetbrains.annotations.NotNull;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public class SetJUC<T> implements Set<T> {

    private final ConcurrentSkipListSet<T> set;

    public SetJUC() {
        set = new ConcurrentSkipListSet<>();
    }

    @Override
    public Iterator<T> iterator() {
        return set.iterator();
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
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean add(T val) {
        set.add(val);
        return true;
    }

    public java.util.Set<T> read() {
        return set;
    }

    @Override
    public boolean remove(Object s) {
        return set.remove(s);
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return false;
    }

    @Override
    public boolean contains(Object val) {
        return set.contains(val);
    }

    @Override
    public boolean addAll(Collection<? extends T> values){

        for (T val: values){
            set.add(val);
        }
        return true;
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

}
