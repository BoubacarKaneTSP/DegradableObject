package eu.cloudbutton.dobj.utils;

import eu.cloudbutton.dobj.register.AtomicWriteOnceReference;
import lombok.Getter;
import lombok.Setter;

public class SegmentAware {

    @Getter
    @Setter
    AtomicWriteOnceReference<Integer> reference;

    public SegmentAware(){
        reference = new AtomicWriteOnceReference<>();
    }

}
