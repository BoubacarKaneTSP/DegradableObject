package eu.cloudbutton.dobj.utils;

import eu.cloudbutton.dobj.incrementonly.BoxedLong;

import java.util.Map;

public class ExtendedSegmentation<T> extends BaseSegmentation<T>{

    FactoryIndice factoryIndice;
    public ExtendedSegmentation(Class<T> clazz, FactoryIndice factoryIndice) {
        super(clazz, factoryIndice);
        this.factoryIndice = factoryIndice;
    }

    @Override
    public final T segmentFor(Object x) {

        SegmentAware obj = (SegmentAware) x;
        T segment;
        BoxedLong indice = obj.getReference().get();

        if (indice == null){
            indice = factoryIndice.getIndice();

            if (!obj.getReference().set(indice)){
                indice = obj.getReference().get();
                assert indice != obj.getReference().get() : indice + " : " + obj.getReference().get();
            }else{
                assert System.identityHashCode(indice) == System.identityHashCode(obj.getReference().get()) : "failed to insert in atomic reference";
            }
        }

//        System.out.println(Thread.currentThread().getName() + " => " + System.identityHashCode(indice) + " : " + indice);

        segment = segments().get((int) indice.getVal());

        for (Object s : ((Map) segment).values()){
            assert s.equals(Thread.currentThread().getName()) : s + " != " + Thread.currentThread().getName();
        }
//        System.out.println(Thread.currentThread().getName() + " => " + indice + " : " + ((Map)segment).values());

        assert segment != null : "Value not associated with a segment";

        return segment;
    }
}
