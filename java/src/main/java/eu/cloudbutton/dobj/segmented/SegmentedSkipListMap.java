package eu.cloudbutton.dobj.segmented;

import eu.cloudbutton.dobj.asymmetric.swmr.map.SWMRSkipListMap;


public class SegmentedSkipListMap<K,V> extends SegmentedOrderedMap<SWMRSkipListMap,K,V> {

    public SegmentedSkipListMap() {
        super(SWMRSkipListMap.class);
    }

}
