package eu.cloudbutton.dobj.segmented;

import eu.cloudbutton.dobj.FactoryIndice;
import eu.cloudbutton.dobj.swsr.SWSRHashSet;
import eu.cloudbutton.dobj.utils.ComposedIterator;
import eu.cloudbutton.dobj.utils.ExtendedSegmentation;
import eu.cloudbutton.dobj.utils.NonLinearizable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class ExtendedSegmentedHashSet<E extends Comparable<E>> extends ExtendedSegmentation<SWSRHashSet> implements Set<E> {

    public ExtendedSegmentedHashSet(FactoryIndice factoryIndice){
        super(SWSRHashSet.class, factoryIndice);
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
        for(SWSRHashSet<E> set: segments()) {
            iterators.add(set.iterator());
        }
        return new ComposedIterator<>(iterators);
    }

    //

    @Override
    @NonLinearizable
    public int size() {
        int ret = 0;
        for(SWSRHashSet<E> set: segments()) {
            ret+=set.size();
        }
        return ret;
    }

    @Override
    @NonLinearizable
    public boolean isEmpty() {
        return size()==0;
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
