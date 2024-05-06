package eu.cloudbutton.dobj.utils;

import jdk.internal.misc.Unsafe;

import java.lang.reflect.Constructor;
import java.sql.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class HashSegmentation<T> implements Segmentation<T>{

    private static final int parallelism = Runtime.getRuntime().availableProcessors();

    protected volatile List<T> segments;

    public HashSegmentation(Class<T> clazz) {
        List<T> list = new ArrayList<>(); // FIXME
        try {
            Constructor<T> constructor = clazz.getConstructor();
            for (int i = 0; i < parallelism; i++) {
                list.add(constructor.newInstance());
            }
            segments = list;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public T segmentFor(Object x) {
        return segments.get(Math.abs(x.hashCode()%parallelism));
    }

}
