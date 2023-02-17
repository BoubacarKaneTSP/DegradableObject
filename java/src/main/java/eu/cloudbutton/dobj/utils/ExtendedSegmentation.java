package eu.cloudbutton.dobj.utils;

public class ExtendedSegmentation<T> extends BaseSegmentation<T>{

    public ExtendedSegmentation(Class<T> clazz, int parallelism) {
        super(clazz, parallelism);
    }

    @Override
    public final T segmentFor(Object x) {

        SegmentAware<T> obj = (SegmentAware<T>) x;
        T segment = obj.getReference().get();
        if (segment==null) {
            segment = local.get();
            obj.getReference().set(segment);
        }
        return segment;
    }
}
