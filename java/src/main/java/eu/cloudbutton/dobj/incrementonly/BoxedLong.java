package eu.cloudbutton.dobj.incrementonly;

import jdk.internal.vm.annotation.Contended;
import lombok.Data;


@Data
@Contended
public class BoxedLong {
    public long val;

    public BoxedLong(){
        val = 0;
    }

    public BoxedLong(long val){
        this.val = val;
    }
}


