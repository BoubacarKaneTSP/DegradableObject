package eu.cloudbutton.dobj.utils;

import jdk.internal.misc.CarrierThreadLocal;
import jdk.internal.vm.annotation.ForceInline;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class ExtendedSegmentation<T> implements Segmentation<T>{

    static private AtomicInteger counter = new AtomicInteger(0);
    static private CarrierThreadLocal<Integer> segmentationIndice = new CarrierThreadLocalWithInitial(
            () -> counter.getAndIncrement());

    protected final List<T> segments;

    public ExtendedSegmentation(Class<T> clazz) {
        this.segments = new ArrayList<>(0);
        for (int i=0; i<Runtime.getRuntime().availableProcessors(); i++) {
            try {
                segments.add(clazz.getDeclaredConstructor().newInstance());
            } catch (Throwable e) {
                throw new RuntimeException();
            }
        }
        Helpers.getUNSAFE().fullFence();
    }

    @Override
    @ForceInline
    public final T segmentFor(Object x) {
        Integer indice = ((SegmentAware) x).getReference().get();
        if (indice == null){
            indice = segmentationIndice.get();
            ((SegmentAware)x).getReference().set(indice);
        }
        return segments.get(indice);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        segments.stream().forEach(segment -> builder.append(segment.toString()));
        return builder.toString();
    }

}
