package eu.cloudbutton.dobj.segmented;

import eu.cloudbutton.dobj.asymmetric.swmr.map.SWMRHashMap;

import java.util.HashMap;

public class SegmentedHashMap<K,V> extends SegmentedMap<HashMap,K,V> {

    public SegmentedHashMap() {
        super(HashMap.class);
    }

}
