package eu.cloudbutton.dobj.asymmetric.swmr;

import org.jetbrains.annotations.NotNull;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class SWMRHashMap<K, V> implements Map<K,V> {

    private final java.util.Map<K, V> m;
    private static final sun.misc.Unsafe UNSAFE;

    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            UNSAFE = (Unsafe) f.get(null);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    public SWMRHashMap() {
        this.m = new java.util.HashMap<>();
    }

    public int size() {
        return this.m.size();
    }

    public boolean isEmpty() {
        return this.m.isEmpty();
    }

    @Override
    public boolean containsKey(Object o) {
        return m.containsKey(o);
    }

    @Override
    public boolean containsValue(Object o) {
        return m.containsValue(o);
    }

    public V get(Object key) {
        return this.m.get(key);
    }

    public V put(K key, V value) {
        V r = this.m.put(key, value);
        UNSAFE.storeFence();
        return r;
    }

    @Override
    public V remove(Object o) {
        V r = this.m.remove(o);
        UNSAFE.storeFence();
        return r;
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> map) {

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

    @NotNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        return null;
    }

    @Override
    public String toString(){
        return m.toString();
    }

}
