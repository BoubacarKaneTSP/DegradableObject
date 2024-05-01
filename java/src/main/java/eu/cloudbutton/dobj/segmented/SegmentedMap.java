package eu.cloudbutton.dobj.segmented;

import eu.cloudbutton.dobj.utils.BaseSegmentation;
import eu.cloudbutton.dobj.utils.ImmutableComposedCollection;
import eu.cloudbutton.dobj.utils.ImmutableComposedSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SegmentedMap<T extends Map,K,V> extends BaseSegmentation<T> implements Map<K,V> {

    public SegmentedMap(Class<T> clazz) {
        super(clazz);
    }

    @Override
    public int size() {
        return segments().stream().mapToInt(segment -> segment.size()).sum();
    }

    @Override
    public boolean isEmpty() {
        return segments().stream().allMatch(segment -> segment.isEmpty());
    }

    @Override
    public boolean containsKey(Object o) {
        return segments().stream().anyMatch(m -> m.containsKey(o));
    }

    @Override
    public boolean containsValue(Object o) {
        return segments().stream().anyMatch(m -> m.containsValue(o));
    }

    @Override
    public V get(Object o) {
        V v = (V) segmentFor(o).get(o);
        if (v!=null) return v;
        for(Map m: segments()){
            v = (V) m.get(o);
            if (v!=null) break;
        }
        return v;
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
        segments().forEach(Map::clear);
    }

    @Override
    public Set<K> keySet() {
        Set<K> ret = new HashSet<>();
        segments().stream().forEach(segment -> ret.addAll(segment.keySet()));
        return ret;
    }

    @NotNull
    @Override
    public Collection<V> values() {
        List<Collection<V>> collections = new ArrayList<>();
        segments().stream().forEach(segment -> collections.add(segment.values()));
        Collection<V> ret = new ImmutableComposedCollection<>(collections);
        return ret;
    }

    @NotNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        throw new UnsupportedOperationException();
    }
}
