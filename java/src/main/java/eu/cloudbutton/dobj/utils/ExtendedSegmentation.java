package eu.cloudbutton.dobj.utils;

import eu.cloudbutton.dobj.incrementonly.BoxedLong;
import eu.cloudbutton.dobj.register.AtomicWriteOnceReference;
import jdk.internal.vm.annotation.ForceInline;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class ExtendedSegmentation<T> implements Segmentation<T>{

    private final List<T> segments;
    private final FactoryIndice factoryIndice;

    public ExtendedSegmentation(Class<T> clazz, FactoryIndice factoryIndice) {
        this.factoryIndice = factoryIndice;
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
            indice = factoryIndice.getIndice();
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
