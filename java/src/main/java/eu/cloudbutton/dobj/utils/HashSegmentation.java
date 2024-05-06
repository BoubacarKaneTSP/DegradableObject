package eu.cloudbutton.dobj.utils;

import java.lang.reflect.Constructor;
import java.util.Map;

public class HashSegmentation<T extends Map> implements Segmentation<T>{

    private static final int parallelism = Runtime.getRuntime().availableProcessors();

    protected volatile Map[] segments;

    public HashSegmentation(Class<T> clazz) {
        segments = new Map[parallelism]; // FIXME
        try {
            Constructor<T> constructor = clazz.getConstructor();
            for (int i = 0; i < parallelism; i++) {
                segments[i] = constructor.newInstance();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public T segmentFor(Object x) {
        return (T) segments[Math.abs(x.hashCode()%parallelism)];
    }

}
