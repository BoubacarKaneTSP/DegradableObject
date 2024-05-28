package eu.cloudbutton.dobj.sharded;

import eu.cloudbutton.dobj.swsr.SWSRSkipListMap;
import eu.cloudbutton.dobj.utils.BaseSegmentation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class ShardedSkipListMap<K,V> extends BaseSegmentation<SWSRSkipListMap> implements Map<K,V> {

    public ShardedSkipListMap() {
        super(SWSRSkipListMap.class);
    }

    @Override
    public V get(Object o) {
        return (V) segmentFor(o).get(o);
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
        throw new IllegalStateException("not supported");
    }

    @Override
    public void clear() {
        throw new IllegalStateException("not supported");
    }

    @NotNull
    @Override
    public Set<K> keySet() {
        throw new IllegalStateException("not supported");
    }

    @NotNull
    @Override
    public Collection<V> values() {
        throw new IllegalStateException("not supported");
    }

    @NotNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        throw new IllegalStateException("not supported");
    }


    @Override
    public int size() {
        int ret = 0;
        for(SWSRSkipListMap<K,V> set: segments) {
            ret+=set.size();
        }
        return ret;

    }

    @Override
    public boolean isEmpty() {
        throw new IllegalStateException("not supported");
    }

    @Override
    public boolean containsKey(Object o) {
        throw new IllegalStateException("not supported");
    }

    @Override
    public boolean containsValue(Object o) {
        throw new IllegalStateException("not supported");
    }

}
