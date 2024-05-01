package eu.cloudbutton.dobj.segmented;

import eu.cloudbutton.dobj.swsr.SWSRTreeSet;

import java.util.*;

// FIXME this should be ordered
public class SegmentedTreeSet<E extends Comparable<E>> extends SegmentedCollection<SWSRTreeSet,E> implements Set<E> {

    public SegmentedTreeSet() {
        super(SWSRTreeSet.class);
    }

}
