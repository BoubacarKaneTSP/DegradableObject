package eu.cloudbutton.dobj.segmented;

import eu.cloudbutton.dobj.asymmetric.swmr.map.SWMRTreeMap;

import java.util.Map;

public class SegmentedTreeMap<K,V> extends SegmentedOrderedMap<SWMRTreeMap,K,V> implements Map<K,V> {

    public SegmentedTreeMap() {
        super(SWMRTreeMap.class);
    }

}
