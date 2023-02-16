package eu.cloudbutton.dobj.utils;

import eu.cloudbutton.dobj.register.AtomicWriteOnceReference;
import lombok.Getter;
import lombok.Setter;

public class SegmentAware<T> {

    @Getter
    @Setter
    AtomicWriteOnceReference<T> reference;

    public SegmentAware(){
        reference = new AtomicWriteOnceReference<>();
    }

}
