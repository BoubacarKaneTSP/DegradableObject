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
        List<T> list = new ArrayList<>(0);
        for (int i=0; i<Runtime.getRuntime().availableProcessors(); i++) {
            try {
                list.add(clazz.getDeclaredConstructor().newInstance());
            } catch (Throwable e) {
                throw new RuntimeException();
            }
        }
        segments = new CopyOnWriteArrayList<>(list);
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
