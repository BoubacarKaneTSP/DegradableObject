package eu.cloudbutton.dobj.segmented;

import eu.cloudbutton.dobj.asymmetric.swmr.map.SWMRSkipListMap;
import eu.cloudbutton.dobj.utils.FactoryIndice;

import java.util.*;

public class ExtendedSegmentedSkipListMap<K,V> extends ExtendedSegmentedMap<SWMRSkipListMap,K,V>  implements Map<K,V> {

    public ExtendedSegmentedSkipListMap(FactoryIndice factoryIndice) {
        super(SWMRSkipListMap.class, factoryIndice);
    }

}
