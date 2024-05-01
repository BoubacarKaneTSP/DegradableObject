package eu.cloudbutton.dobj.segmented;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class SegmentedOrderedMap<T extends Map, K,V> extends SegmentedMap<T,K,V> {

    public SegmentedOrderedMap(Class clazz) {
        super(clazz);
    }

    @Override
    public Set<K> keySet() {
        Set<K> ret = new TreeSet<>();
        segments().stream().forEach(segment -> ret.addAll(segment.keySet()));
        return ret;
    }

}
