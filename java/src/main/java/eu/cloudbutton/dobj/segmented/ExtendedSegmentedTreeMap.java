package eu.cloudbutton.dobj.segmented;

import eu.cloudbutton.dobj.asymmetric.swmr.map.SWMRTreeMap;

import java.util.*;

// FIXME this should be ordered
public class ExtendedSegmentedTreeMap<K,V> extends ExtendedSegmentedMap<SWMRTreeMap,K,V>  implements Map<K,V> {

    public ExtendedSegmentedTreeMap() {
        super(SWMRTreeMap.class);
    }

}
