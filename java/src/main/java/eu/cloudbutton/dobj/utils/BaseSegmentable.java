package eu.cloudbutton.dobj.utils;

import eu.cloudbutton.dobj.register.AtomicWriteOnceReference;
import jdk.internal.vm.annotation.ForceInline;

public class BaseSegmentable implements Segmentable{

    private AtomicWriteOnceReference<Integer> reference;

    public BaseSegmentable() {
        reference = new AtomicWriteOnceReference<>();
    }

    @Override
    @ForceInline
    public Integer getIndice() {
        return reference.get();
    }

    @Override
    public void setIndice(int indice) {
        reference.set(indice);
    }
}
