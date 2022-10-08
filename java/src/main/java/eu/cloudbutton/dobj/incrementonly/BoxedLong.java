package eu.cloudbutton.dobj.incrementonly;

import lombok.Data;

@Data
@jdk.internal.vm.annotation.Contended
public class BoxedLong {

    public long val;

    public BoxedLong(){
        val = 0;
    }
}


