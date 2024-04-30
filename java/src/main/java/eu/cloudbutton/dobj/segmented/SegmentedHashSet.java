package eu.cloudbutton.dobj.segmented;

import eu.cloudbutton.dobj.asymmetric.swmr.SWMRHashSet;
import eu.cloudbutton.dobj.set.ConcurrentHashSet;
import eu.cloudbutton.dobj.utils.FactoryIndice;
import eu.cloudbutton.dobj.swsr.SWSRHashSet;
import eu.cloudbutton.dobj.utils.BaseSegmentation;
import eu.cloudbutton.dobj.utils.ComposedIterator;
import eu.cloudbutton.dobj.utils.NonLinearizable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class SegmentedHashSet<E extends Comparable<E>> extends BaseSegmentation<SWMRHashSet> implements Set<E>, Iterable<E> {
    
    public SegmentedHashSet(int parallelism){
        super(SWMRHashSet.class, parallelism);
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
    @NonLinearizable
    public Iterator<E> iterator() {
        Collection<Iterator<E>> iterators = new ArrayList<>();
        for(Set<E> set: segments()) {
            iterators.add(set.iterator());
        }
        return new ComposedIterator<E>(iterators);
    }

    //

    @Override
    @NonLinearizable
    public int size() {
        return segments().stream().mapToInt(Set::size).sum();
    }

    @Override
    @NonLinearizable
    public boolean isEmpty() {
        return segments().stream().allMatch(Set::isEmpty);
    }

    @Override
    public boolean contains(Object o) {
        return segments().stream().anyMatch(set -> set.contains(o));
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
        segments().stream().forEach(s -> s.clear());
    }
}
