package eu.cloudbutton.dobj.segmented;

import eu.cloudbutton.dobj.asymmetric.swmr.SWMRSkipListSet;
import java.util.Set;

public class SegmentedSkipListSet<E extends Comparable<E>> extends SegmentedCollection<SWMRSkipListSet,E> implements Set<E> {

    public SegmentedSkipListSet() {
        super(SWMRSkipListSet.class);
    }

}
