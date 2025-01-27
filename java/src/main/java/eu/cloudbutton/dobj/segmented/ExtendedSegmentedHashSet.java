package eu.cloudbutton.dobj.segmented;

import eu.cloudbutton.dobj.asymmetric.swmr.SWMRHashSet;
import eu.cloudbutton.dobj.set.ConcurrentHashSet;

import java.util.Set;

public final class ExtendedSegmentedHashSet<E> extends ExtendedSegmentedCollection<SWMRHashSet,E> implements Set<E> {

    public ExtendedSegmentedHashSet(){
        super(SWMRHashSet.class);
    }

}