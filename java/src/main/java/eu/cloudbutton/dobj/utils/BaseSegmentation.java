package eu.cloudbutton.dobj.utils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class BaseSegmentation<T> implements Segmentation<T> {

    private final ThreadLocal<T> local;
    private final AtomicInteger next;

    private final List<T> segments;
    private final Class<T> clazz;

    public BaseSegmentation(Class<T> clazz, int parallelism) {
        this.clazz = clazz;
        this.segments = new ArrayList<>(parallelism);
        for(int i = 0; i<parallelism; i++ ){
            try {
                this.segments.add(clazz.getDeclaredConstructor().newInstance());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        this.next = new AtomicInteger(0);
        this.local = ThreadLocal.withInitial(() -> {return segments.get(next.getAndIncrement()%parallelism);});
    }

    @Override
    public final T segmentFor(Object x) {
        return local.get();
    }

    @Override
    public final Collection<T> segments(){
        return segments;
    }

}
