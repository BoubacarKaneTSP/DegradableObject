package eu.cloudbutton.dobj.segmented;

import eu.cloudbutton.dobj.asymmetric.swmr.SWMRHashSet;
import eu.cloudbutton.dobj.set.ConcurrentHashSet;

import java.util.Set;

public class SegmentedHashSet<E> extends SegmentedCollection<ConcurrentHashSet,E> implements Set<E> {
    
    public SegmentedHashSet(){
        super(ConcurrentHashSet.class);
    }

}
