package eu.cloudbutton.dobj.utils;

import eu.cloudbutton.dobj.FactoryIndice;

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
        Integer indice = obj.getReference().get();

        if (indice == null){
            indice = factoryIndice.getIndice();
            if (!obj.getReference().set(indice)){
                indice = obj.getReference().get();
            }
        }

        segment = segments().get(indice);

        assert segment != null : "Value not associated with a segment";

        return segment;
    }
}
