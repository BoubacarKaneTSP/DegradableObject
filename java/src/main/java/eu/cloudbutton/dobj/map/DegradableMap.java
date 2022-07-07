package eu.cloudbutton.dobj.map;

import lombok.SneakyThrows;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class DegradableMap<K,V> extends AbstractMap<K,V> {

    private final List<HashMap<K,V>> listMap;
    private final ThreadLocal<HashMap<K,V>> local;

    public DegradableMap(){
        listMap = new CopyOnWriteArrayList<>();
        local = ThreadLocal.withInitial(() -> {
            HashMap<K, V> m = new HashMap<>();
            listMap.add(m);
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

    @SneakyThrows
    @Override
    public V get(Object key) {
        V value;

        value = local.get().get(key);

        if (value != null)
            return value;

        for (AbstractMap<K,V> map : listMap){
            value = map.get(key);
            if (value != null)
                return value;
        }
        throw new NullPointerException();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return null;
    }

    @Override
    public int size(){
        int size = 0;

        for (AbstractMap<K,V> map: listMap){
            size += map.size();
        }

        return size;
    }
}
