package eu.cloudbutton.dobj.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class BaseSegmentation<T> implements Segmentation<T> {

    public static Method currentCarrierThread;
    static {
        try {
            currentCarrierThread = Thread.class.getDeclaredMethod("currentCarrierThread");
            currentCarrierThread.setAccessible(true);
            } catch (Exception e) {
                throw new Error(e);
            }
    }

    protected final ScopedValue<T> segment = ScopedValue.newInstance();

    private Class<T> clazz;

    private final AtomicInteger next;

    private final List<T> segments;

    private final int parallelism;

    public BaseSegmentation(Class<T> clazz, int parallelism) {
        this.parallelism = Runtime.getRuntime().availableProcessors();
        this.clazz = clazz;
        this.segments = new ArrayList<>();
        for(int i=0; i< this.parallelism; i++) this.segments.add(null);
        this.next = new AtomicInteger(0);
    }

    @Override
    public final T segmentFor(Object x) {
        int index = carrierID()%parallelism; // FIXME no collision?
        System.out.println(this.parallelism + " : "+ this.segments.size());
        if ( segments.get(index) == null ) {
            try {
                this.segments.set(index, clazz.getDeclaredConstructor().newInstance());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new Error(e);
            }
        }
        return ScopedValue.where(segment, segments.get(index)).get(segment);
    }

    @Override
    public final List<T> segments(){
        return segments;
    }

    public static final int carrierID() {
        try {
            return (int) ((Thread)currentCarrierThread.invoke(null)).getId();
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

}
