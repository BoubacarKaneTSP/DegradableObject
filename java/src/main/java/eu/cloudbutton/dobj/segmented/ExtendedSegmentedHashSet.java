package eu.cloudbutton.dobj.segmented;

import eu.cloudbutton.dobj.set.ConcurrentHashSet;

import java.util.Set;

public class ExtendedSegmentedHashSet<E> extends ExtendedSegmentedCollection<ConcurrentHashSet,E> implements Set<E> {

    public ExtendedSegmentedHashSet(){
        super(ConcurrentHashSet.class);
    }

}