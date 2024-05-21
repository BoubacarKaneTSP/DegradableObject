package eu.cloudbutton.dobj.segmented;

import eu.cloudbutton.dobj.asymmetric.swmr.map.SWMRSkipListMap;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

// FIXME this should be ordered
public class ExtendedSegmentedSkipListMap<K,V> extends ExtendedSegmentedMap<ConcurrentSkipListMap,K,V>  implements Map<K,V> {

    public ExtendedSegmentedSkipListMap() {
        super(ConcurrentSkipListMap.class);
    }

}
