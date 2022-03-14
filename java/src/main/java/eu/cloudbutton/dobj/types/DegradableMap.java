package eu.cloudbutton.dobj.types;

import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DegradableMap<K,V> extends AbstractMap<K,V> {

    private final ConcurrentMap<Thread, ConcurrentHashMap<K,V>> map;
    private final ThreadLocal<ConcurrentHashMap<K,V>> local;

    public DegradableMap(){
        map = new ConcurrentHashMap<>();
        this.local = ThreadLocal.withInitial(() -> {
            ConcurrentHashMap<K, V> m = new ConcurrentHashMap<>();
            map.put(Thread.currentThread(),m);
            return m;
        });
    }

    @Override
    public V put(K key, V value) {
        return local.get().put(key, value);
    }

    @Override
    public V remove(Object key) {
        return local.get().remove(key);
    }

    @Override
    public V get(Object key) {
        return local.get().get(key);
    }

    @NotNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        return null;
    }
}
