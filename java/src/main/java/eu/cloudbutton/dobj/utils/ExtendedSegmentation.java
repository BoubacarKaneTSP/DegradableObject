package eu.cloudbutton.dobj.utils;

import eu.cloudbutton.dobj.incrementonly.BoxedLong;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ExtendedSegmentation<T> implements Segmentation<T>{

    private final List<T> segments;
    private final FactoryIndice factoryIndice;

    public ExtendedSegmentation(Class<T> clazz, FactoryIndice factoryIndice) {
        this.factoryIndice = factoryIndice;
        int parallelism = factoryIndice.getParallelism()   ;
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
    public final T segmentFor(Object x) {

        SegmentAware obj = (SegmentAware) x;
        T segment;
        BoxedLong indice = obj.getReference().get();

        System.out.println(Thread.currentThread().getName() + " is adding");

        if (indice == null){
            indice = factoryIndice.getIndice();

            if (!obj.getReference().set(indice)){
                indice = obj.getReference().get();
                assert indice != obj.getReference().get() : indice + " : " + obj.getReference().get();
            }else{
                assert System.identityHashCode(indice) == System.identityHashCode(obj.getReference().get()) : "failed to insert in atomic reference";
            }
        }

        System.out.println(Thread.currentThread().getName() + " => " + System.identityHashCode(indice) + " : " + indice);

        segment = segments().get((int) indice.getVal());

        System.out.println(Thread.currentThread().getName() + " => " + indice + " : " + ((Map)segment).values());

        assert segment != null : "Value not associated with a segment";

        return segment;
    }

    @Override
    public List<T> segments() {
        return segments;
    }
}
