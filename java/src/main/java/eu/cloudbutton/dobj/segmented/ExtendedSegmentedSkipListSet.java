package eu.cloudbutton.dobj.segmented;

import eu.cloudbutton.dobj.asymmetric.swmr.SWMRSkipListSet;

import java.util.Set;

public class ExtendedSegmentedSkipListSet<E> extends ExtendedSegmentedCollection<SWMRSkipListSet,E> implements Set<E> {
    public ExtendedSegmentedSkipListSet() {super(SWMRSkipListSet.class);}
}
