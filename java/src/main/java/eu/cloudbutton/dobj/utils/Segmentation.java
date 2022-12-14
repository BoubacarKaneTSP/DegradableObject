package eu.cloudbutton.dobj.utils;

import java.util.Collection;

public interface Segmentation<T> {

    T segmentFor(Object x);

    Collection<T> segments();
}