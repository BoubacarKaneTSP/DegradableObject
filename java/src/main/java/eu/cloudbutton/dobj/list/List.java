package eu.cloudbutton.dobj.list;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class List<T> extends AbstractList<T> {
    private final CopyOnWriteArrayList<T> list;

    public List(CopyOnWriteArrayList<T> list) {
        this.list = list;
    }

    public List() {
        list = new CopyOnWriteArrayList<>();
    }

    @Override
    public boolean add(T t) {
        return list.add(t);
    }

    @Override
    public boolean remove(Object o) {
        return list.remove(o);
    }

    @Override
    public T remove(int index) {
        return list.remove(index);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        List<?> list1 = (List<?>) o;
        return Objects.equals(list, list1.list);
    }

    @Override
    public T get(int index) {
        return list.get(index);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return list.addAll(c);
    }

    @Override
    public int size() {
        return list.size();
    }
}
