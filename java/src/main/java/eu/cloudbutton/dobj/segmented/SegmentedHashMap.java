package eu.cloudbutton.dobj.segmented;

import eu.cloudbutton.dobj.asymmetric.swmr.SWMRHashMap;
import eu.cloudbutton.dobj.utils.BaseSegmentation;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class SegmentedHashMap<K,V> extends BaseSegmentation<SWMRHashMap> implements Map<K,V> {

    public SegmentedHashMap(int parallelism) {
        super(SWMRHashMap.class, parallelism);
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
        V v = null;

        for(SWMRHashMap m: segments()){
            if (m.containsKey(o)){
                v = (V) m.get(o);
                return v;
            }
        }

        if (v == null) {
            TimeUnit.SECONDS.sleep(10);
            for(SWMRHashMap<K,V> m: segments()){
                System.out.println("v = " + m.get(o));
                v = m.get(o);
                if (v!=null) break;
            }
            System.out.println();
            System.out.println("Thread " + Thread.currentThread().getName() + " is trying to get : " + o );
            System.out.println("value associated with " + o + " : " + v);
            for(SWMRHashMap<K,V> m: segments()){
                System.out.println(m.keySet());
                System.out.println(m.containsKey(o));
                System.out.println(m.get(o));
                System.out.println();
            }
            System.exit(0);
        }
        return null;
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
