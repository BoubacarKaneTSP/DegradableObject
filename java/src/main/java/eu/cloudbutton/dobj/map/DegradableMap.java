package eu.cloudbutton.dobj.map;

import eu.cloudbutton.dobj.counter.BoxLong;
import lombok.Getter;
import lombok.SneakyThrows;
import org.javatuples.Pair;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class DegradableMap<K,V> extends AbstractMap<K,V> {

    @Getter
    private final List<Map<K,V>> listMap;
    private final ThreadLocal<Map<K,V>> local;
    private final ConcurrentHashMap<K, Map<K,V>> mapIndex;

    public DegradableMap(){
        listMap = new CopyOnWriteArrayList<>();
        mapIndex = new ConcurrentHashMap<>();
        local = ThreadLocal.withInitial(() -> {
            ConcurrentHashMap<K, V> m = new ConcurrentHashMap<>();
            listMap.add(m);
            return m;
        });

    }

    @Override
    public V put(K key, V value) {
        V ret = local.get().put(key, value);
        if (ret==null)
            mapIndex.put(key,local.get());
        return ret;
    }

    @Override
    public V remove(Object key) {
        V ret = local.get().remove(key);
        mapIndex.remove(key);
        return ret;
    }

    @SneakyThrows
    @Override
    public V get(Object key) {

        if (key == null)
            throw new NullPointerException();

        V value;

        value = local.get().get(key);

        if (value != null)
            return value;

        Map<K,V> map = mapIndex.get(key);
        if (map != null)
            return map.get(key);

        for (Map<K,V> amap : listMap){
            if (amap != local.get()){
                value = amap.get(key);
                if (value != null)
                    return value;
            }
        }

        return null;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return null;
    }

    @Override
    public int size(){
        int size = 0;

        for (Map<K,V> map: listMap){
            size += map.size();
        }

        return size;
    }
}
