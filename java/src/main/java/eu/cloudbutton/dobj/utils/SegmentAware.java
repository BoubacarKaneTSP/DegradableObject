package eu.cloudbutton.dobj.utils;

import eu.cloudbutton.dobj.incrementonly.BoxedLong;
import eu.cloudbutton.dobj.register.AtomicWriteOnceReference;
import jdk.internal.vm.annotation.Contended;
import lombok.Getter;
import lombok.Setter;

public class SegmentAware {

    @Getter
    @Setter
    @Contended
    AtomicWriteOnceReference<BoxedLong> reference;

    public SegmentAware(){
        reference = new AtomicWriteOnceReference<>();
    }

}
