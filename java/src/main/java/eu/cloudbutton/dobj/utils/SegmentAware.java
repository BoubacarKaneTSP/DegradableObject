package eu.cloudbutton.dobj.utils;

import eu.cloudbutton.dobj.incrementonly.BoxedLong;
import eu.cloudbutton.dobj.register.AtomicWriteOnceReference;
import jdk.internal.vm.annotation.Contended;
import jdk.internal.vm.annotation.ForceInline;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.atomic.AtomicReference;

public class SegmentAware {

    private AtomicReference<Integer> reference;

    public SegmentAware(){
        reference = new AtomicReference<>();
    }

    @ForceInline
    public final AtomicReference<Integer> getReference() {
        return reference;
    }

    public final void setReference(AtomicReference<Integer> reference) {
        this.reference = reference;
    }
}
