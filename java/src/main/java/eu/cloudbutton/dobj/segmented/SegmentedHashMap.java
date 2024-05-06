package eu.cloudbutton.dobj.segmented;

import eu.cloudbutton.dobj.asymmetric.swmr.map.SWMRHashMap;

import java.util.Hashtable;

public class SegmentedHashMap<K,V> extends SegmentedMap<Hashtable,K,V> {

    public SegmentedHashMap() {
        super(Hashtable.class);
    }

}
