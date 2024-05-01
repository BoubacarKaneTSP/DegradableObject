package eu.cloudbutton.dobj.segmented;

import java.util.*;

public class ExtendedSegmentedTreeMap<K,V> extends ExtendedSegmentedMap<HashMap,K,V>  implements Map<K,V> {

    public ExtendedSegmentedTreeMap() {
        super(HashMap.class);
    }

}
