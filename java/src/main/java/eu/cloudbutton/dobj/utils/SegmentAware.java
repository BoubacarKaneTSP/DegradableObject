package eu.cloudbutton.dobj.utils;

import eu.cloudbutton.dobj.register.AtomicWriteOnceReference;
import lombok.Getter;
import lombok.Setter;

public class SegmentAware<T> {

    @Getter
    @Setter
    AtomicWriteOnceReference<T> segment;

    public SegmentAware(){
        segment = new AtomicWriteOnceReference<>();
    }

}
