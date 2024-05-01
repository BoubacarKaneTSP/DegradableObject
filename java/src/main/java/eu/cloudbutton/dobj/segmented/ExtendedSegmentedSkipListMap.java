package eu.cloudbutton.dobj.segmented;

import eu.cloudbutton.dobj.asymmetric.swmr.map.SWMRSkipListMap;

import java.util.*;

public class ExtendedSegmentedSkipListMap<K,V> extends ExtendedSegmentedMap<SWMRSkipListMap,K,V>  implements Map<K,V> {

    public ExtendedSegmentedSkipListMap() {
        super(SWMRSkipListMap.class);
    }

}
