package eu.cloudbutton.dobj.swsr;

import org.jetbrains.annotations.NotNull;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.*;

public class SWSRTreeSet<E>  extends AbstractSet<E> implements Set<E> {

    private static final sun.misc.Unsafe UNSAFE;

    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            UNSAFE = (Unsafe) f.get(null);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    Set<E> m;

    public SWSRTreeSet(){
        m = new TreeSet<>();
    }

    @Override
    public Iterator<E> iterator() {
        return m.iterator();
    }

    @Override
    public int size() {
        return m.size();
    }

    @NotNull
    @Override
    public Object[] toArray() {
        return m.toArray();
    }

    @NotNull
    @Override
    public <T> T[] toArray(@NotNull T[] a) {
        return m.toArray(a);
    }

    @Override
    public boolean add(E e) {
        final boolean add = m.add(e);
        UNSAFE.storeFence();
        return add;
    }

    @Override
    public boolean remove(Object o) {
        final boolean remove = m.remove(o);
        UNSAFE.storeFence();
        return remove;
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return m.containsAll(c);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends E> c) {
        final boolean b = m.addAll(c);
        UNSAFE.storeFence();
        return b;
    }

    @Override
    public boolean removeAll(@NotNull Collection c) {
        final boolean b = m.removeAll(c);
        UNSAFE.storeFence();
        return b;
    }

    @Override
    public boolean retainAll(@NotNull Collection c) {
        final boolean b = m.retainAll(c);
        UNSAFE.storeFence();
        return b;
    }

    @Override
    public void clear() {
        m.clear();
    }

    @Override
    public Spliterator<E> spliterator() {
        return m.spliterator();
    }

    @Override
    public boolean isEmpty() {
        return m.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return m.contains(o);
    }
}
