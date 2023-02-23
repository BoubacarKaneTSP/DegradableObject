package eu.cloudbutton.dobj.utils;

import eu.cloudbutton.dobj.incrementonly.BoxedLong;
import eu.cloudbutton.dobj.register.AtomicWriteOnceReference;
import lombok.Getter;
import lombok.Setter;

public class SegmentAware {

    @Getter
    @Setter
    AtomicWriteOnceReference<BoxedLong> reference;

    public SegmentAware(){
        reference = new AtomicWriteOnceReference<>();
    }

}
