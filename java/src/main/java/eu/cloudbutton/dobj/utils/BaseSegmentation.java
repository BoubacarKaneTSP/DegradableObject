package eu.cloudbutton.dobj.utils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class BaseSegmentation<T> implements Segmentation<T> {

    private final List<T> segments;

    public BaseSegmentation(Class<T> clazz, FactoryIndice factoryIndice) {
        int parallelism = factoryIndice.getParallelism();
        this.segments = new ArrayList<>(parallelism);

        try {
            for (int i = 0; i < parallelism; i++) {
                this.segments.add(i, clazz.getDeclaredConstructor().newInstance());
                assert segments.get(i) != null : "Class not added to segment";
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }

        assert segments.size() == parallelism : "Wrong number of segments";
    }

    @Override
    public T segmentFor(Object x) {
        T segment = null;
        
        try{
//            segment = local.get();
        }catch (NullPointerException e){
            e.printStackTrace();
            System.out.println("Failed to get a segment for " + x);
            System.exit(0);
        }

        return segment;
    }

    @Override
    public final List<T> segments(){
        return segments;
    }

}
