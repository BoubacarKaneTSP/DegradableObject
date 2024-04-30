package eu.cloudbutton.dobj.asymmetric;

import eu.cloudbutton.dobj.incrementonly.Counter;
import eu.cloudbutton.dobj.incrementonly.CounterIncrementOnly;

public class CounterMISD extends CounterIncrementOnly implements Counter {

    public CounterMISD(){
        super();
    }

    @Override
    public long decrementAndGet(){
        segmentFor(null).val -=1 ;
        UNSAFE.storeFence();

        return 0;
    }

}
