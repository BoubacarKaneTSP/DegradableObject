package eu.cloudbutton.dobj.utils;


import jdk.internal.vm.annotation.ForceInline;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

import jdk.internal.misc.*;

public class BaseSegmentation<T> implements Segmentation<T> {

    private final CopyOnWriteArrayList<T> segments;
    private final CarrierThreadLocal<T> local;
    private final Class<T> clazz;

    public BaseSegmentation(Class<T> clazz) {
        this.segments = new CopyOnWriteArrayList<>();
        this.local = new CarrierThreadLocalWithInitial<>(
                () -> {
                    try {
                        T segment = clazz.getDeclaredConstructor().newInstance();
                        segments.add(segment);
                        return segment;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
        );
        this.clazz = clazz;
    }

    @Override
    @ForceInline
    public T segmentFor(Object x) {
        return local.get();
    }

    @Override
    @ForceInline
    public final Collection<T> segments() {
        return segments;
    }

}
