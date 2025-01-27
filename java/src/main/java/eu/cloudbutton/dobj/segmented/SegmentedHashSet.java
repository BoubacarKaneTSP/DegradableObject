package eu.cloudbutton.dobj.segmented;

import eu.cloudbutton.dobj.asymmetric.swmr.SWMRHashSet;

import java.util.HashSet;
import java.util.Set;

public final class SegmentedHashSet<E> extends SegmentedCollection<SWMRHashSet,E> implements Set<E> {
    
    public SegmentedHashSet(){
        super(SWMRHashSet.class);
    }

}
