package eu.cloudbutton.dobj.segmented;

import java.util.Set;
import java.util.TreeSet;

public class SegmentedSkipListSet<E extends Comparable<E>> extends SegmentedCollection<TreeSet,E> implements Set<E> {

    public SegmentedSkipListSet() {
        super(TreeSet.class);
    }

}
