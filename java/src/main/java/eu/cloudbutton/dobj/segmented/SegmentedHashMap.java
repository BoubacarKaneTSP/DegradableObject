package eu.cloudbutton.dobj.segmented;

import eu.cloudbutton.dobj.asymmetric.swmr.map.SWMRHashMap;
import eu.cloudbutton.dobj.utils.BaseSegmentation;
import eu.cloudbutton.dobj.utils.ImmutableComposedCollection;
import eu.cloudbutton.dobj.utils.ImmutableComposedSet;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class SegmentedHashMap<K,V> extends BaseSegmentation<SWMRHashMap> implements Map<K,V> {

    public SegmentedHashMap() {
        super(SWMRHashMap.class);
    }

    @Override
    public int size() { // FIXME prove this is actually linearizable
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

    @SneakyThrows
    @Override
    public V get(Object o) {
        V v = (V) segmentFor(o).get(o);
        for(Map m: segments()){
            if (v!=null) break;
            v = (V) m.get(o);
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
        List<Set<K>> sets = new ArrayList<>();
        segments().stream().forEach(segment -> sets.add(segment.keySet()));
        Set<K> ret = new ImmutableComposedSet<>(sets);
        return ret;
    }

    @Override
    public String toString() { // FIXME
        StringBuilder s = new StringBuilder();
        for(Map m: segments()){
            s.append(m.toString());
        }
        return s.toString();
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
