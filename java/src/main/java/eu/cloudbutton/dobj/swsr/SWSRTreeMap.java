package eu.cloudbutton.dobj.swsr;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class SWSRTreeMap<K,V> extends AbstractMap<K,V> implements Map<K,V> {

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

    Map<K,V> m;
    
    public  SWSRTreeMap(){
        m = new TreeMap<>();
    }
    
    @Override
    public int size() {
        return m.size();
    }

    @Override
    public boolean isEmpty() {
        return m.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return m.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return m.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return m.get(key);
    }

    @Nullable
    @Override
    public V put(K key, V value) {
        final V v = m.put(key,value);
        UNSAFE.storeFence();
        return v;
    }

    @Override
    public V remove(Object key) {
        final V v = m.remove(key);
        UNSAFE.storeFence();
        return v;
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> c) {
        m.putAll(c);
        UNSAFE.storeFence();
    }

    @Override
    public void clear() {
        m.clear();
    }

    @NotNull
    @Override
    public Set<K> keySet() {
        return m.keySet();
    }

    @NotNull
    @Override
    public Collection<V> values() {
        return m.values();
    }

    @NotNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        return m.entrySet();
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        return m.getOrDefault(key, defaultValue);
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        m.forEach(action);
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        m.replaceAll(function);
        UNSAFE.storeFence();
    }

    @Nullable
    @Override
    public V putIfAbsent(K key, V value) {
        final V v = m.putIfAbsent(key, value);
        UNSAFE.storeFence();
        return v;
    }

    @Override
    public boolean remove(Object key, Object value) {
        final boolean b = m.remove(key, value);
        UNSAFE.storeFence();
        return b;
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        final boolean b = m.replace(key, oldValue, newValue);
        UNSAFE.storeFence();
        return b;
    }

    @Nullable
    @Override
    public V replace(K key, V value) {
        final V v = m.replace(key, value);
        UNSAFE.storeFence();
        return v;
    }

    @Override
    public V computeIfAbsent(K key, @NotNull Function<? super K, ? extends V> mappingFunction) {
        final V v = m.computeIfAbsent(key, mappingFunction);
        UNSAFE.storeFence();
        return v;
    }

    @Override
    public V computeIfPresent(K key, @NotNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        final V v = m.computeIfPresent(key, remappingFunction);
        UNSAFE.storeFence();
        return v;
    }

    @Override
    public V compute(K key, @NotNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        V v = m.compute(key, remappingFunction);
        UNSAFE.storeFence();
        return v;
    }

    @Override
    public V merge(K key, @NotNull V value, @NotNull BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        final V merge = m.merge(key, value, remappingFunction);
        UNSAFE.storeFence();
        return merge;
    }
}
