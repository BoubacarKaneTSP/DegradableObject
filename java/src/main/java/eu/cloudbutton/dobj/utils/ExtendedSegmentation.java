package eu.cloudbutton.dobj.utils;

public class ExtendedSegmentation<T> extends BaseSegmentation<T>{

    public ExtendedSegmentation(Class<T> clazz, int parallelism) {
        super(clazz, parallelism);
    }

    @Override
    public final T segmentFor(Object x) {

        SegmentAware<T> obj = (SegmentAware<T>) x;
        T segment;
        segment = local.get();

        try{
            obj.getSegment().set(segment);
        }catch (NullPointerException e){
            e.printStackTrace();
            System.out.println("Failed to get a segment for " + x);
            System.exit(0);
        }catch (IllegalStateException e){
            segment = obj.getSegment().get();
        }

        return segment;
    }
}
