package eu.cloudbutton.dobj.segmented;

import eu.cloudbutton.dobj.asymmetric.swmr.map.SWMRHashMap;
import eu.cloudbutton.dobj.juc.ConcurrentHashMap;

public class SegmentedHashMap<K,V> extends SegmentedMap<SWMRHashMap,K,V> {

    public SegmentedHashMap() {
        super(SWMRHashMap.class);
    }

}
