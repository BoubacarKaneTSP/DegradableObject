package eu.cloudbutton.dobj.segmented;

import eu.cloudbutton.dobj.asymmetric.swmr.SWSRSkipListSet;
import eu.cloudbutton.dobj.utils.ComposedIterator;
import eu.cloudbutton.dobj.utils.BaseSegmentation;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SegmentedTreeSet<E extends Comparable<E>> extends BaseSegmentation<SWSRSkipListSet> implements Set<E> {

    public SegmentedTreeSet(int parallelism) {
        super(SWSRSkipListSet.class, parallelism);
    }

    @Override
    public boolean add(E e) {
        return segmentFor(e).add(e);
    }

    @Override
    public boolean remove(Object o) {
        return segmentFor(o).remove(o);
    }

    @NotNull
    @Override
    public Iterator<E> iterator() {
        Collection<Iterator<E>> iterators = new ArrayList<>();
        for(SWSRSkipListSet<E> set: segments()) {
            iterators.add(set.iterator());
        }
        return new ComposedIterator<E>(iterators);
    }

    //

    @Override
    public int size() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(Object o) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public <T> T[] toArray(@NotNull T[] ts) {
         throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> collection) {
         throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends E> collection) {
         throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> collection) {
         throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> collection) {
         throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
         throw new UnsupportedOperationException();
    }

}
