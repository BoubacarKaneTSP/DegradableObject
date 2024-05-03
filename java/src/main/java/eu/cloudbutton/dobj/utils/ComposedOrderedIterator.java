package eu.cloudbutton.dobj.utils;

import java.util.Collection;
import java.util.Iterator;

public class ComposedOrderedIterator<E> implements Iterator<E> {

    private Iterator<Iterator<E>> _inUnion;
    private Iterator<E> _inMap;
    private Collection<E> first;

    public ComposedOrderedIterator(final Collection<Iterator<E>> collection) {
        assert collection != null;
        if (!collection.isEmpty()){
            _inUnion = collection.iterator();
            _inMap = _inUnion.next();
        }
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public E next() {
        return null;
    }
}
