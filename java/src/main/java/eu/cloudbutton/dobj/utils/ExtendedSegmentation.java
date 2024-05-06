package eu.cloudbutton.dobj.utils;

import jdk.internal.misc.CarrierThreadLocal;
import jdk.internal.vm.annotation.ForceInline;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class ExtendedSegmentation<T> implements Segmentation<T>{

    static private AtomicInteger counter = new AtomicInteger(0);
    static private CarrierThreadLocal<Integer> indices = new CarrierThreadLocalWithInitial(
            () -> counter.getAndIncrement());

    protected final List<T> segments;

    public ExtendedSegmentation(Class<T> clazz) {
        segments = new CopyOnWriteArrayList<>();
        for (int i=0; i<Runtime.getRuntime().availableProcessors(); i++) {
            try {
                segments.add(clazz.getDeclaredConstructor().newInstance());
            } catch (Throwable e) {
                throw new RuntimeException();
            }
        }
    }

    @Override
    @ForceInline
    public final T segmentFor(Object x) {
        Integer indice = ((Segmentable) x).getIndice();
        if (indice == null){
            indice = indices.get();
            ((Segmentable)x).setIndice(indice);
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
