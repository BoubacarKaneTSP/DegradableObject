package eu.cloudbutton.dobj.sharded;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ShardedMap<K,V> implements Map<K,V> {

    private final ThreadLocal<Map<K,V>> local;

    public ShardedMap(){
        local = ThreadLocal.withInitial(() -> {
            Map<K, V> m = new HashMap<>();
            return m;
        });
    }
    @Override
    public int size() {
        return local.get().size();
    }

    @Override
    public boolean isEmpty() {
        return local.get().isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return local.get().containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return local.get().containsKey(value);
    }

    @Override
    public V get(Object key) {
        return local.get().get(key);
    }

    @Nullable
    @Override
    public V put(K key, V value) {
        return local.get().put(key,value);
    }

    @Override
    public V remove(Object key) {
        return local.get().remove(key);
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> m) {
        local.get().putAll(m);
    }

    @Override
    public void clear() {
        local.get().clear();
    }

    @NotNull
    @Override
    public Set<K> keySet() {
        return local.get().keySet();
    }

    @NotNull
    @Override
    public Collection<V> values() {
        return local.get().values();
    }

    @NotNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        return local.get().entrySet();
    }
}
