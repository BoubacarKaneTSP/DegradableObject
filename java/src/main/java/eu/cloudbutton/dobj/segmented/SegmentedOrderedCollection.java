package eu.cloudbutton.dobj.segmented;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;

public class SegmentedOrderedCollection<T extends Collection,E> extends SegmentedCollection<T,E> {

    public SegmentedOrderedCollection(Class clazz) {
        super(clazz);
    }

    @Override
    public Iterator<E> iterator() {
        // WIP
        return null;
    }
}
