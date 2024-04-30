package eu.cloudbutton.dobj.segmented;

import eu.cloudbutton.dobj.utils.BaseSegmentation;
import eu.cloudbutton.dobj.utils.ComposedIterator;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class SegmentedCollection<T extends Collection,E> extends BaseSegmentation<T> implements Collection<E> {

    public SegmentedCollection(Class<T> clazz) {
        super(clazz);
    }

    @Override
    public int size() {
        return segments().stream().mapToInt(Collection::size).sum();
    }

    @Override
    public boolean isEmpty() {
        return segments().stream().allMatch(Collection::isEmpty);
    }

    @Override
    public boolean contains(Object o) {
        return segments().stream().anyMatch(segment -> segment.contains(o));
    }

    @Override
    public Iterator<E> iterator() {
        Collection<Iterator<E>> iterators = new ArrayList<>(segments().size());
        for(Collection<E> set: segments()) {
            iterators.add(set.iterator());
        }
        return new ComposedIterator<E>(iterators);
    }

    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T[] toArray(@NotNull T[] a) {
        throw new UnsupportedOperationException();    }

    @Override
    public boolean add(E e) {
        return segmentFor(e).add(e);
    }

    @Override
    public boolean remove(Object o) {
        return segmentFor(o).remove(o);
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return segments().stream().allMatch(segment -> segment.containsAll(c));
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends E> c) {
        throw new UnsupportedOperationException();    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException();    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException();    }

    @Override
    public void clear() {
        segments().stream().forEach(Collection::clear);
    }
}
