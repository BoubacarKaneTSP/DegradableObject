package eu.cloudbutton.dobj.segmented;

import eu.cloudbutton.dobj.asymmetric.swmr.map.SWMRHashMap;

public class SegmentedHashMap<K,V> extends SegmentedMap<SWMRHashMap,K,V> {

    public SegmentedHashMap() {
        super(SWMRHashMap.class);
    }

}
