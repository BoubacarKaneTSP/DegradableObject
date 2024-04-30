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

    private static final sun.misc.Unsafe UNSAFE;

    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            UNSAFE = (Unsafe) f.get(null);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    private final List<T> segments;
    private final FactoryIndice factoryIndice;

    public ExtendedSegmentation(Class<T> clazz, FactoryIndice factoryIndice) {
        this.factoryIndice = factoryIndice;
        this.segments = new ArrayList<>();
        for (int i=0; i<Runtime.getRuntime().availableProcessors(); i++) {
            try {
                T segment = clazz.getDeclaredConstructor().newInstance();
                segments.add(segment);
            } catch (Throwable e) {
                e.printStackTrace();
                throw new RuntimeException();
            }
        }
        UNSAFE.storeFence();
    }

    @Override
    public final T segmentFor(Object x) {
        SegmentAware obj = (SegmentAware) x;
        T segment;
        Integer indice = obj.getReference().get();
        if (indice == null){
            indice = factoryIndice.getIndice();
            if (!obj.getReference().set(indice)){
                assert false : x;
                // indice = obj.getReference().get();
            }
        }
        segment = segments().get(indice);
        return segment;
    }

    @Override
    @ForceInline
    public final List<T> segments() {
        return segments;
    }
}
