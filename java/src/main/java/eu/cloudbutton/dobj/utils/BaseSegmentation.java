package eu.cloudbutton.dobj.utils;

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
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    protected final ScopedValue<T> segment = ScopedValue.newInstance();

    private final AtomicInteger next;

    private final List<T> segments;

    private final int parallelism;

    public BaseSegmentation(Class<T> clazz, int parallelism) {
        this.segments = new ArrayList<>(parallelism);
        this.parallelism = Runtime.getRuntime().availableProcessors();
        for(int i = 0; i<this.parallelism; i++ ){
            try {
                this.segments.add(clazz.getDeclaredConstructor().newInstance());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        this.next = new AtomicInteger(0);
    }

    @Override
    public final T segmentFor(Object x) {
        int index = carrierID()%parallelism;
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
