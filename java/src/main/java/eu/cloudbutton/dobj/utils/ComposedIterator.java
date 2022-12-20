package eu.cloudbutton.dobj.utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

public final class ComposedIterator<E> implements Iterator<E> {
    
    private Iterator<Iterator<E>> _inUnion;
    private Iterator<E> _inMap;

    public ComposedIterator(final Collection<Iterator<E>> collection) {
        if (!collection.isEmpty()){
            _inUnion = collection.iterator();
            _inMap = _inUnion.next();
        }
    }

    @Override
    public boolean hasNext() {
        if (_inUnion == null) return false;
        if (!_inMap.hasNext()) {
            do {
                if (!_inUnion.hasNext()) return false;
                _inMap = _inUnion.next();
            } while (!_inMap.hasNext());
        }
        return true;
    }

    @Override
    public E next() {
        if (!_inMap.hasNext()) {
            do {
                if (!_inUnion.hasNext()) throw new NoSuchElementException();
                _inMap = _inUnion.next();
            } while (!_inMap.hasNext());
        }
        return _inMap.next();
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

}
