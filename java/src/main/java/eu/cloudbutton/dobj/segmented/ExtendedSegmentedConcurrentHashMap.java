package eu.cloudbutton.dobj.segmented;

import eu.cloudbutton.dobj.mcwmcr.MapAddIntensive;
import eu.cloudbutton.dobj.utils.ComposedIterator;
import eu.cloudbutton.dobj.utils.FactoryIndice;
import eu.cloudbutton.dobj.utils.ExtendedSegmentation;
import eu.cloudbutton.dobj.utils.NonLinearizable;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ExtendedSegmentedConcurrentHashMap<K,V> extends ExtendedSegmentation<ConcurrentHashMap> implements Map<K,V> {

    public ExtendedSegmentedConcurrentHashMap(FactoryIndice factoryIndice) {
        super(ConcurrentHashMap.class, factoryIndice);
    }

    public static class KeyIterator<K,V> implements Iterator<K> {

        Iterator<Map<K,V>> _inUnion;
        Iterator<K> _inMap;
        Collection<Map<K,V>> _elements;

        public KeyIterator(Collection<Map<K,V>> elts) {

            _elements = elts;

            Iterator<Map<K,V>> itr = _elements.iterator();

            if (itr.hasNext()){
                _inUnion = _elements.iterator();
                _inMap = _inUnion.next().keySet().iterator();
            }
        }

        public boolean hasNext() {

            if (_inUnion == null) return false;

            if (!_inMap.hasNext()) {
                do {
                    if (!_inUnion.hasNext()) return false;
                    _inMap = _inUnion.next().keySet().iterator();
                } while (!_inMap.hasNext());
            }
            return true;
        }

        public K next() {
            if (!_inMap.hasNext()) {
                do {
                    if (!_inUnion.hasNext()) throw new NoSuchElementException();
                    _inMap = _inUnion.next().keySet().iterator();
                } while (!_inMap.hasNext());
            }
            return _inMap.next();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

    public static class ValueIterator<K,V> implements Iterator<V> {

        Iterator<Map<K,V>> _inUnion;
        Iterator<V> _inMap;
        Collection<Map<K,V>> _elements;

        public ValueIterator(Collection<Map<K,V>> elts) {

            _elements = elts;


            Iterator<Map<K,V>> itr = _elements.iterator();

            if (itr.hasNext()){
                _inUnion = _elements.iterator();
                _inMap = _inUnion.next().values().iterator();
            }
        }

        public boolean hasNext() {

            if (_inUnion == null) return false;

            if (!_inMap.hasNext()) {
                do {
                    if (!_inUnion.hasNext()) return false;
                    _inMap = _inUnion.next().values().iterator();
                } while (!_inMap.hasNext());
            }
            return true;
        }

        public V next() {
            if (!_inMap.hasNext()) {
                do {
                    if (!_inUnion.hasNext()) throw new NoSuchElementException();
                    _inMap = _inUnion.next().values().iterator();
                } while (!_inMap.hasNext());
            }
            return _inMap.next();
        }

    }

    public Iterator<K> iterator() {
        return new KeyIterator(segments());
    }

    @Override
    public String toString() {
        String ret = "";
        for(Map m: segments()){
            ret += m.toString();
        }
        return ret;
    }

    @Override
    public int size() { // FIXME prove this is actually linearizable
        int ret = 0;
        for(Map m: segments()){
            ret += m.size();
        }
        return ret;
    }

    @Override
    public boolean isEmpty() {
        for(Map m: segments()){
            if (!m.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public boolean containsKey(Object o) {
        for(Map m: segments()){
            if (m.containsKey(o)) return true;
        }
        return false;
    }

    @Override
    public boolean containsValue(Object o) {
        for(Map m: segments()){
            if (m.containsValue(o)) return true;
        }
        return false;
    }

    @SneakyThrows
    @Override
    public V get(Object o) {
        Map map = segmentFor(o);

        return (V) map.get(o);
    }

    @Nullable
    @Override
    public V put(K k, V v) {
        return (V) segmentFor(k).put(k,v);
    }

    @Override
    public V remove(Object o) {
        return (V) segmentFor(o).remove(o);
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> map) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Set<K> keySet() {
        Set<K> ret = new HashSet<>();
        for(Map m: segments()){
            ret.addAll(m.keySet());
        }
        return ret;
    }

    @NotNull
    @Override
    public Collection<V> values() {
        List<V> ret = new ArrayList<>();
        for(Map m: segments()){
            ret.addAll(m.values());
        }
        return ret;
    }

    @NotNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        Set<Entry<K, V>> ret = new HashSet<>();
        for(Map m: segments()){
            ret.addAll(m.entrySet());
        }
        return ret;
    }

}
