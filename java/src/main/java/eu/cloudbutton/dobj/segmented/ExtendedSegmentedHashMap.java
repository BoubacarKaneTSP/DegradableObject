package eu.cloudbutton.dobj.segmented;

import eu.cloudbutton.dobj.asymmetric.swmr.map.SWMRHashMap;
import eu.cloudbutton.dobj.juc.ConcurrentHashMap;

import java.util.*;

public class ExtendedSegmentedHashMap<K,V> extends ExtendedSegmentedMap<SWMRHashMap,K,V> implements Map<K,V> {

    public ExtendedSegmentedHashMap() {
        super(SWMRHashMap.class);
    }
}
