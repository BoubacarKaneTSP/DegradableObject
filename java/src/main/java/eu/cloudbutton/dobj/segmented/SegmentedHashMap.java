package eu.cloudbutton.dobj.segmented;

import eu.cloudbutton.dobj.asymmetric.swmr.SWMRHashMap;
import eu.cloudbutton.dobj.utils.BaseSegmentation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

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

    @Override
    public V get(Object o) {
        V v = null;
        try{
            for(SWMRHashMap m: segments()){
                v = (V) m.get(o);
                if (v!=null) break;;
            }
        }catch (NullPointerException e){
            e.printStackTrace();
            System.out.println("NullPointerException in map.get()");
            System.exit(0);
        }

        if (v == null) {
            System.out.println("trying to get : " + o );
            System.out.println("segments : " + segments());

            System.exit(0);
        }
        return v;
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
