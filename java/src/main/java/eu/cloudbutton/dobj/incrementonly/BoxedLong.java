package eu.cloudbutton.dobj.incrementonly;

import lombok.Data;

@Data
public class BoxedLong {

    public long val;

    public BoxedLong(){
        val = 0;
    }
}
