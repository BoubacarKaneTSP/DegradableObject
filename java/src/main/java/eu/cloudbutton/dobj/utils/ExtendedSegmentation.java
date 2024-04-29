package eu.cloudbutton.dobj.utils;

import eu.cloudbutton.dobj.incrementonly.BoxedLong;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class ExtendedSegmentation<T> implements Segmentation<T>{

    private final List<T> segments;
    private final FactoryIndice factoryIndice;

    public ExtendedSegmentation(Class<T> clazz, FactoryIndice factoryIndice) {
        this.factoryIndice = factoryIndice;
        this.segments = new CopyOnWriteArrayList<>();
        for (int i=0; i<Runtime.getRuntime().availableProcessors(); i++) {
            try {
                T segment = clazz.getDeclaredConstructor().newInstance();
                segments.add(segment);
            } catch (Throwable e) {
                e.printStackTrace();
                throw new RuntimeException();
            }
        }
    }

    @Override
    public final T segmentFor(Object x) {
        SegmentAware obj = (SegmentAware) x;
        T segment;
        BoxedLong indice = obj.getReference().get();
        if (indice == null){
            indice = factoryIndice.getIndice();
            if (!obj.getReference().set(indice)){
                assert false : x;
                // indice = obj.getReference().get();
            }
        }
        segment = segments().get((int) indice.getVal());
        return segment;
    }

    @Override
    public final List<T> segments() {
        return segments;
    }
}
