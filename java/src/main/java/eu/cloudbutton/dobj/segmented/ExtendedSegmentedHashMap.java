package eu.cloudbutton.dobj.segmented;

import eu.cloudbutton.dobj.utils.FactoryIndice;

import java.util.*;

public class ExtendedSegmentedHashMap<K,V> extends ExtendedSegmentedMap<Hashtable,K,V> implements Map<K,V> {

    public ExtendedSegmentedHashMap(FactoryIndice factoryIndice) {
        super(Hashtable.class, factoryIndice);
    }
}
