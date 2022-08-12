package eu.cloudbutton.dobj.map;

import lombok.SneakyThrows;
import org.javatuples.Pair;

import java.util.AbstractMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class DegradableMap<K,V> extends AbstractMap<K,V> {

    private final List<ConcurrentHashMap<K,V>> listMap;
    private final ThreadLocal<ConcurrentHashMap<K,V>> local;
    private final List<List<Pair<K,V>>> mapView;

    // The default size of the tab
    private static final int DEFAULT_SIZE = 1000000;

    public DegradableMap(){
        listMap = new CopyOnWriteArrayList<>();
        mapView = new CopyOnWriteArrayList<>();
        for (int i = 0; i < DEFAULT_SIZE; i++) {
            mapView.add(new CopyOnWriteArrayList<>());
        }
        local = ThreadLocal.withInitial(() -> {
            ConcurrentHashMap<K, V> m = new ConcurrentHashMap<>();
            listMap.add(m);
            return m;
        });
    }

    @Override
    public V put(K key, V value) {

        mapView.get(key.hashCode()%DEFAULT_SIZE).add(new Pair<>(key,value));
        return  local.get().put(key, value);
    }

    @Override
    public V remove(Object key) {
        return local.get().remove(key);
    }

    @SneakyThrows
    @Override
    public V get(Object key) {

        if (key == null)
            throw new NullPointerException();

        for (Pair<K,V> pair: mapView.get(key.hashCode()%DEFAULT_SIZE)){
            if (pair.getValue0().equals(key))
                return pair.getValue1();
        }

/*        V value;

        value = local.get().get(key);

        if (value != null)
            return value;

        for (AbstractMap<K,V> map : listMap){
            value = map.get(key);
            if (value != null)
                return value;
        }*/

        return null;
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
