package eu.cloudbutton.dobj.asymmetric;

import eu.cloudbutton.dobj.incrementonly.Counter;
import eu.cloudbutton.dobj.incrementonly.CounterIncrementOnly;

public class CounterMISD extends CounterIncrementOnly implements Counter {

    public CounterMISD(){
        super();
    }

    public void decrement(){
        local.get().val -=1 ;
        UNSAFE.storeFence();
    }
}
