package eu.cloudbutton.dobj.segmented;

import eu.cloudbutton.dobj.asymmetric.swmr.map.SWMRHashMap;
import eu.cloudbutton.dobj.utils.ExtendedSegmentation;
import eu.cloudbutton.dobj.utils.HashSegmentation;
import org.jetbrains.annotations.NotNull;
import java.util.function.BiFunction;

import java.util.*;
import java.util.stream.Stream;

public class ExtendedSegmentedMap<T extends Map, K, V> extends ExtendedSegmentation<T> implements Map<K, V> {

    public ExtendedSegmentedMap(Class<T> clazz) {
        super(clazz);
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
        Collection<Map<K,V>> list = new ArrayList<>();
        segments.stream().forEach(list::add);
        return new KeyIterator(list);
    }

    @Override
    public int size() {
        return segments.stream().mapToInt(s -> s.size()).sum();
    }

    @Override
    public boolean isEmpty() {
        return segments.stream().allMatch(s -> s.isEmpty());
    }

    @Override
    public boolean containsKey(Object o) {
        return get(o) != null;
    }

    @Override
    public boolean containsValue(Object o) {
        return segments.stream().anyMatch(s -> s.containsValue(o));
    }

    @Override
    public V get(Object o) {
        return (V) segmentFor(o).get(o);
    }

    @Override
    public V put(K k, V v) {
        return (V) segmentFor(k).put(k,v);
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return (V) segmentFor(key).compute(key,remappingFunction);
    }

    @Override
    public V remove(Object o) {
        return (V) segmentFor(o).remove(o);
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> map) {
        map.forEach((k,v)->{this.put(k,v);});
    }

    @Override
    public void clear() {
        segments.stream().forEach(s -> s.clear());
    }

    @NotNull
    @Override
    public Set<K> keySet() {
        Set<K> ret = new HashSet<>();
        for(Map m: segments){
            ret.addAll(m.keySet());
        }
        return ret;
    }

    @NotNull
    @Override
    public Collection<V> values() {
        List<V> ret = new ArrayList<>();
        for(Map m: segments){
            ret.addAll(m.values());
        }
        return ret;
    }

    @NotNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        Set<Entry<K, V>> ret = new HashSet<>();
        for(Map m: segments){
            ret.addAll(m.entrySet());
        }
        return ret;
    }

    public int getNbTreeBin(){
        int nbTreeBin = 0;

        for(Map m: segments){
            nbTreeBin += ((SWMRHashMap) m).getNbTreeBin();
        }

        return nbTreeBin;
    }

    public int getNbBin(){
        int nbBin = 0;

        for(Map m: segments){
            nbBin += ((SWMRHashMap) m).getNbBin();
        }

        return nbBin;
    }
}
