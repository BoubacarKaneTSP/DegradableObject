package eu.cloudbutton.dobj.Set;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;

public class DegradableSet<T> extends AbstractSet<T> {

    private final CopyOnWriteArrayList<ConcurrentSkipListSet<T>> set;
    private final ThreadLocal<ConcurrentSkipListSet<T>> local;

    public DegradableSet() {
        this.set = new CopyOnWriteArrayList<>();
        this.local = ThreadLocal.withInitial(() -> {
            ConcurrentSkipListSet<T> l = new ConcurrentSkipListSet<>();
            set.add(l);
            return l;
        });
    }

    public static class setsIterator<V> implements Iterator<V> {

        Iterator<ConcurrentSkipListSet<V>> _inUnion;
        Iterator<V> _inSet;
        Collection<ConcurrentSkipListSet<V>> _elements;

        public setsIterator(Collection<ConcurrentSkipListSet<V>> elts) {

            _elements = elts;


            Iterator<ConcurrentSkipListSet<V>> itr = _elements.iterator();

            if (itr.hasNext()){
                _inUnion = _elements.iterator();
                _inSet = _inUnion.next().iterator();
            }
        }

        public boolean hasNext() {

            if (_inUnion == null) return false;

            if (!_inSet.hasNext()) {
                do {
                    if (!_inUnion.hasNext()) return false;
                    _inSet = _inUnion.next().iterator();
                } while (!_inSet.hasNext());
            }
            return false;
        }

        public V next() {
            if (!_inSet.hasNext()) {
                do {
                    if (!_inUnion.hasNext()) throw new NoSuchElementException();
                    _inSet = _inUnion.next().iterator();
                } while (!_inSet.hasNext());
            }
            return _inSet.next();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

    @Override
    public Iterator<T> iterator() {

        /*CopyOnWriteArrayList<ConcurrentSkipListSet<T>> copy = new CopyOnWriteArrayList();
        copy.add(new ConcurrentSkipListSet<>());*/
        return new setsIterator<>(set);
    }

    @Override
    public int size() {
        int size = 0;
        for (AbstractSet s: set){
            size += s.size();
        }
        return size;
    }

    @Override
    public boolean add(T val) {
        return local.get().add(val);
    }

    public java.util.Set<T> read() {
        java.util.Set<T> result = new HashSet<>();
        for (ConcurrentSkipListSet<T> val : set){
            result.addAll(val);
        }
        return result;
    }

    @Override
    public boolean remove(Object val) {
        return local.get().remove(val);
    }

    @Override
    public boolean contains(Object val) {
        for (ConcurrentSkipListSet<T> s : set){
            if (s.contains(val)) return true;
        }
        return false;
    }

    @Override
    public String toString(){
        return "method toString not build yet";
    }

}
