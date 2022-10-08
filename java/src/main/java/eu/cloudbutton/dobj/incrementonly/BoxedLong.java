package eu.cloudbutton.dobj.incrementonly;

import lombok.Data;

@Data
public class BoxedLong {

//    @jdk.internal.vm.annotation.Contended
    public long val;

    public BoxedLong(){
        val = 0;
    }
}


