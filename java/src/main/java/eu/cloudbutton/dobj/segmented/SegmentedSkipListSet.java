package eu.cloudbutton.dobj.segmented;

import eu.cloudbutton.dobj.swsr.SWSRSkipListSet;
import eu.cloudbutton.dobj.swsr.SWSRSkipListSet;
import eu.cloudbutton.dobj.utils.ComposedIterator;
import eu.cloudbutton.dobj.utils.BaseSegmentation;
import eu.cloudbutton.dobj.utils.NonLinearizable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SegmentedSkipListSet<E extends Comparable<E>> extends BaseSegmentation<SWSRSkipListSet> implements Set<E> {

    public SegmentedSkipListSet(int parallelism) {
        super(SWSRSkipListSet.class, parallelism);
    }

    @Override
    public boolean add(E e) {
        boolean b = false;
        try{
            b = segmentFor(e).add(e);
        }catch (NullPointerException ex){
            ex.printStackTrace();
            System.out.println("Failed to add : " + e);
            System.exit(0);
        }
        return b;
    }

    @Override
    public boolean remove(Object o) {
        boolean b = false;
        try{
            b = segmentFor(o).remove(o);
        }catch (NullPointerException e){
            e.printStackTrace();
            System.out.println("Failed to remove : " + o);
            System.exit(0);
        }
        return b;
    }

    @NotNull
    @Override
    @NonLinearizable
    public Iterator<E> iterator() {
        Collection<Iterator<E>> iterators = new ArrayList<>();
        for(SWSRSkipListSet<E> set: segments()) {
            iterators.add(set.iterator());
        }
        return new ComposedIterator<E>(iterators);
    }

    //

    @Override
    @NonLinearizable
    public int size() {
        int ret = 0;
        for(SWSRSkipListSet<E> set: segments()) {
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
