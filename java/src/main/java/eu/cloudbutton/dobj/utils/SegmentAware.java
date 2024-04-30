package eu.cloudbutton.dobj.utils;

import eu.cloudbutton.dobj.incrementonly.BoxedLong;
import eu.cloudbutton.dobj.register.AtomicWriteOnceReference;
import jdk.internal.vm.annotation.Contended;
import jdk.internal.vm.annotation.ForceInline;
import lombok.Getter;
import lombok.Setter;

public class SegmentAware {

    private AtomicWriteOnceReference<Integer> reference;

    public SegmentAware(){
        reference = new AtomicWriteOnceReference<>();
    }

    @ForceInline
    public final AtomicWriteOnceReference<Integer> getReference() {
        return reference;
    }

    public final void setReference(AtomicWriteOnceReference<Integer> reference) {
        this.reference = reference;
    }
}
