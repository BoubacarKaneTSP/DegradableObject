package eu.cloudbutton.dobj.map;

import eu.cloudbutton.dobj.counter.BoxLong;
import lombok.Getter;
import lombok.SneakyThrows;
import org.javatuples.Pair;

import java.util.AbstractMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class DegradableMap<K,V> extends AbstractMap<K,V> {

    @Getter
    private final List<ConcurrentHashMap<K,V>> listMap;
    private final ThreadLocal<ConcurrentHashMap<K,V>> local;
//    private final ThreadLocal<BoxLong> index;
//    private final ConcurrentHashMap<K, Integer> mapIndex;

    public DegradableMap(){
        listMap = new CopyOnWriteArrayList<>();
//        mapIndex = new ConcurrentHashMap<>();
       /* index = ThreadLocal.withInitial(() -> {
            BoxLong boxLong = new BoxLong();
            boxLong.setVal(-1);
            return boxLong;
        });
*/
        local = ThreadLocal.withInitial(() -> {
            ConcurrentHashMap<K, V> m = new ConcurrentHashMap<>();
            listMap.add(m);
            return m;
        });

    }

    @Override
    public V put(K key, V value) {
/*
        if (index.get().getVal() == -1) {
           *//* for (AbstractMap map: listMap){
                System.out.println(map);
            }*//*
            index.get().setVal(listMap.indexOf(local.get()));
            System.out.println(listMap.get((int) index.get().getVal()) == local.get() );
//            System.out.println("size listmap : "+listMap.size());
//            System.out.println(index.get());
        }

        mapIndex.put(key, (int) index.get().getVal());*/

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
/*

        if (key == null || mapIndex.get(key) == null)
            throw new NullPointerException();
*/


        V value;

        value = local.get().get(key);

        if (value != null)
            return value;

        for (AbstractMap<K,V> map : listMap){
            if (map != local.get()){
                value = map.get(key);
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

        for (AbstractMap<K,V> map: listMap){
            size += map.size();
        }

        return size;
    }
}
