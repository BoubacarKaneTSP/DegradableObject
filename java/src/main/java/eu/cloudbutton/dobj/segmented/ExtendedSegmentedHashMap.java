package eu.cloudbutton.dobj.segmented;

import eu.cloudbutton.dobj.FactoryIndice;
import eu.cloudbutton.dobj.asymmetric.swmr.map.SWMRHashMap;
import eu.cloudbutton.dobj.utils.ExtendedSegmentation;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class ExtendedSegmentedHashMap<K,V> extends ExtendedSegmentation<SWMRHashMap> implements Map<K,V> {

    public ExtendedSegmentedHashMap(FactoryIndice factoryIndice) {
        super(SWMRHashMap.class, factoryIndice);
    }

    @Override
    public int size() { // FIXME prove this is actually linearizable
        int ret = 0;
        for(SWMRHashMap m: segments()){
            ret += m.size();
        }
        return ret;
    }

    @Override
    public boolean isEmpty() {
        for(SWMRHashMap m: segments()){
            if (!m.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public boolean containsKey(Object o) {
        for(SWMRHashMap m: segments()){
            if (m.containsKey(o)) return true;
        }
        return false;
    }

    @Override
    public boolean containsValue(Object o) {
        for(SWMRHashMap m: segments()){
            if (m.containsValue(o)) return true;
        }
        return false;
    }

    @SneakyThrows
    @Override
    public V get(Object o) {
        SWMRHashMap map = segmentFor(o);

        return (V) map.get(o);
    }

    @Nullable
    @Override
    public V put(K k, V v) {
        SWMRHashMap map = segmentFor(k);

        for (Object s : map.values()){
//            System.out.println(s + " => " + Thread.currentThread().getName());
            assert s.equals(Thread.currentThread().getName()) : s + " != " + Thread.currentThread().getName();
        }

        return (V) map.put(k,v);
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
