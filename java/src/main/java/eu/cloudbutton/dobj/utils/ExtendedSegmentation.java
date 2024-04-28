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
        int parallelism = factoryIndice.getParallelism();
        this.segments = new CopyOnWriteArrayList<>();
        try {
            for (int i = 0; i < parallelism; i++) {
                this.segments.add(i, clazz.getDeclaredConstructor().newInstance());
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }

        assert segments.size() == parallelism : "Wrong number of segments";
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
    public List<T> segments() {
        return segments;
    }
}
