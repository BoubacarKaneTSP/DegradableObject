package eu.cloudbutton.dobj.segmented;

import eu.cloudbutton.dobj.utils.FactoryIndice;

import java.util.*;

public class ExtendedSegmentedTreeMap<K,V> extends ExtendedSegmentedMap<HashMap,K,V>  implements Map<K,V> {

    public ExtendedSegmentedTreeMap(FactoryIndice factoryIndice) {
        super(HashMap.class, factoryIndice);
    }

}
