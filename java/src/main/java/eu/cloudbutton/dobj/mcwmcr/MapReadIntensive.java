package eu.cloudbutton.dobj.mcwmcr;

import lombok.Getter;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class MapReadIntensive<K,V> implements Map<K,V> {

    @Getter
    private final List<Map<K,V>> listMap;
    private final ThreadLocal<Map<K,V>> local;
    private final Map<K, Map<K,V>> mapIndex;

    public MapReadIntensive(){
        listMap = new CopyOnWriteArrayList<>();
        mapIndex = new ConcurrentHashMap<>();
        local = ThreadLocal.withInitial(() -> {
            ConcurrentHashMap<K, V> m = new ConcurrentHashMap<>();
            listMap.add(m);
            return m;
        });
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
        return new KeyIterator<>(listMap);
    }

    @Override
    public V put(K key, V value) {

        if (key == null || value == null)
            throw new NullPointerException();

        V ret = local.get().put(key, value);
        if (ret==null)
            mapIndex.put(key,local.get());
        return ret;
    }

    @Override
    public V remove(Object key) {
        if (key == null)
            throw new NullPointerException();
        V ret = local.get().remove(key);
        if (ret != null)
            mapIndex.remove(key);
        return ret;
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> m) {

    }

    @Override
    public void clear() {

    }

    @NotNull
    @Override
    public Set<K> keySet() {
        return null;
    }

    @NotNull
    @Override
    public Collection<V> values() {
        return null;
    }

    @SneakyThrows
    @Override
    public V get(Object key) {

        if (key == null)
            throw new NullPointerException();

        V value;

        value = local.get().get(key);

        if (value != null)
            return value;

        Map<K,V> map = mapIndex.get(key);
        if (map != null)
            return map.get(key);

        for (Map<K,V> amap : listMap){
            if (amap != local.get()){
                value = amap.get(key);
                if (value != null)
                    return value;
            }
        }

        return null;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return null;
    }

    @Override
    public int size(){
        int size = 0;

        for (Map<K,V> map: listMap){
            size += map.size();
        }

        return size;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean containsKey(Object key) {
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }
}
