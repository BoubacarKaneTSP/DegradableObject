package eu.cloudbutton.dobj.segmented;

import eu.cloudbutton.dobj.asymmetric.swmr.SWMRHashSet;

import java.util.Set;

public class SegmentedHashSet<E> extends SegmentedCollection<SWMRHashSet,E> implements Set<E> {
    
    public SegmentedHashSet(){
        super(SWMRHashSet.class);
    }

}
