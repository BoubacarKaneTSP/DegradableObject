package eu.cloudbutton.dobj.utils;

import jdk.internal.misc.CarrierThreadLocal;
import jdk.internal.vm.annotation.ForceInline;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class ExtendedSegmentation<T> implements Segmentation<T>{

    private final List<T> segments;
    static private AtomicInteger counter = new AtomicInteger(0);
    static private CarrierThreadLocal<Integer> segmentationIndice = new CarrierThreadLocalWithInitial(
            () -> {return counter.getAndIncrement();});

    public ExtendedSegmentation(Class<T> clazz) {
        List<T> list = new ArrayList<>();
        for (int i=0; i<Runtime.getRuntime().availableProcessors(); i++) {
            try {
                T segment = clazz.getDeclaredConstructor().newInstance();
                list.add(segment);
            } catch (Throwable e) {
                throw new RuntimeException();
            }
        }
        this.segments = new CopyOnWriteArrayList<>(list);
    }

    @Override
    public final T segmentFor(Object x) {
        SegmentAware obj = (SegmentAware) x;
        Integer indice = obj.getReference().get();
        if (indice == null){
            indice = segmentationIndice.get();
            if (!obj.getReference().set(indice)){
                assert false : x;
            }
        }
        return segments.get(indice);
    }

    @Override
    @ForceInline
    public final List<T> segments() {
        return segments;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        segments().stream().forEach(segment -> builder.append(segment.toString()));
        return builder.toString();
    }

}
