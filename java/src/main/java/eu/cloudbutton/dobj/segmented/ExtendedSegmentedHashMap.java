package eu.cloudbutton.dobj.segmented;

import eu.cloudbutton.dobj.asymmetric.swmr.map.SWMRHashMap;
import eu.cloudbutton.dobj.juc.ConcurrentHashMap;

import java.util.*;

public class ExtendedSegmentedHashMap<K,V> extends ExtendedSegmentedMap<HashMap,K,V> implements Map<K,V> {

    public ExtendedSegmentedHashMap() {
        super(HashMap.class);
    }
}
