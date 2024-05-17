package eu.cloudbutton.dobj.utils;

import jdk.internal.misc.CarrierThreadLocal;
import jdk.internal.vm.annotation.ForceInline;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class ExtendedSegmentation<T> implements Segmentation<T>{

    private static AtomicInteger counter = new AtomicInteger(0);
    private static CarrierThreadLocal<Integer> indices = new CarrierThreadLocalWithInitial(
            () -> counter.getAndIncrement());
    private static final int parallelism = Runtime.getRuntime().availableProcessors();

    protected final List<T> segments;

    public ExtendedSegmentation(Class<T> clazz) {
        segments = new CopyOnWriteArrayList<>();
        for (int i=0; i<parallelism; i++) {
            try {
                segments.add(clazz.getDeclaredConstructor().newInstance());
            } catch (Throwable e) {
                e.printStackTrace();
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
