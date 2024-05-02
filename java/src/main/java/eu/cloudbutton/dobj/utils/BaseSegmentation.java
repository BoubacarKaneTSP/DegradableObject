package eu.cloudbutton.dobj.utils;


import jdk.internal.vm.annotation.ForceInline;

import java.util.concurrent.CopyOnWriteArrayList;

import jdk.internal.misc.*;

public class BaseSegmentation<T> implements Segmentation<T> {

    protected final CopyOnWriteArrayList<T> segments;
    private final CarrierThreadLocal<T> local;

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
    }

    @Override
    @ForceInline
    public T segmentFor(Object x) {
        return local.get();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        segments.stream().forEach(segment -> builder.append(segment.toString()));
        return builder.toString();
    }

}
