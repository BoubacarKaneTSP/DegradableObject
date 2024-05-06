package eu.cloudbutton.dobj.utils;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class HashSegmentation<T> implements Segmentation<T>{

    private static final int parallelism = Runtime.getRuntime().availableProcessors();

    protected List<T> segments;

    public HashSegmentation(Class<T> clazz) {
        segments = new CopyOnWriteArrayList<>(); // FIXME
        try {
            Constructor<T> constructor = clazz.getConstructor();
            for (int i = 0; i < parallelism; i++) {
                segments.add(constructor.newInstance());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public T segmentFor(Object x) {
        return segments.get(Math.abs(x.hashCode()%parallelism));
    }

}
