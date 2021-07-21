package eu.cloudbutton.dobj.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class List<T> extends AbstractList<T> {

    private final java.util.List<T> list;
/*    private AtomicInteger size;
    private static int bound = 10000;*/

    public List() {
        list = new ArrayList<>();
//        size = new AtomicInteger(0);
    }

    @Override
    public void append(T val) {
/*        if (size.get() <= bound) {
            size.incrementAndGet();
        }else if (size.get() >= bound){
            list.clear();
            size.set(1);
        }*/
        list.add(val);
    }

    @Override
    public java.util.List<T> read() {

        java.util.List result;

        result = Arrays.asList(list.toArray());

        return result;
    }

    @Override
    public boolean remove(T val) {
        return list.remove(val);
    }

    @Override
    public boolean contains(T val) {
        return list.contains(val);
    }
}
