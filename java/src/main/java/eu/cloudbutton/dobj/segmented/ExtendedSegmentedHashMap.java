package eu.cloudbutton.dobj.segmented;

import eu.cloudbutton.dobj.asymmetric.swmr.map.SWMRHashMap;

import java.util.*;

public class ExtendedSegmentedHashMap<K,V> extends ExtendedSegmentedMap<Hashtable,K,V> implements Map<K,V> {

    public ExtendedSegmentedHashMap() {
        super(Hashtable.class);
    }
}
