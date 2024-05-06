package eu.cloudbutton.dobj.utils;

import eu.cloudbutton.dobj.register.AtomicWriteOnceReference;

public class BaseSegmentable implements Segmentable{

    private AtomicWriteOnceReference<Integer> reference;

    public BaseSegmentable() {
        reference = new AtomicWriteOnceReference<>();
    }

    @Override
    public Integer getIndice() {
        return reference.get();
    }

    @Override
    public void setIndice(int indice) {
        reference.set(indice);
    }
}
