package eu.cloudbutton.dobj.segmented;

import eu.cloudbutton.dobj.asymmetric.swmr.map.SWMRSkipListMap;
import eu.cloudbutton.dobj.utils.FactoryIndice;
import eu.cloudbutton.dobj.swsr.SWSRSkipListMap;
import eu.cloudbutton.dobj.utils.BaseSegmentation;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;


public class SegmentedSkipListMap<K,V> extends BaseSegmentation<SWMRSkipListMap> implements Map<K,V> {

    public SegmentedSkipListMap(int parallelism) {
        super(SWMRSkipListMap.class, parallelism);
    }
    
    @Override
    public int size() {
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
        V v = null;
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
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Set<K> keySet() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Collection<V> values() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        throw new UnsupportedOperationException();
    }
}
