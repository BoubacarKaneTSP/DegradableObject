package eu.cloudbutton.dobj.asymmetric;

import eu.cloudbutton.dobj.incrementonly.BoxedLong;
import eu.cloudbutton.dobj.incrementonly.Counter;
import eu.cloudbutton.dobj.incrementonly.CounterIncrementOnly;

public class CounterMISD extends CounterIncrementOnly implements Counter {

    ThreadLocal<BoxedLong> threadLocal;
    public CounterMISD(){
        super();
        threadLocal = ThreadLocal.withInitial(() -> new BoxedLong());
    }

    @Override
    public long decrementAndGet(){
//        threadLocal.get().val -= 1;
        local.get().val -=1 ;
        UNSAFE.storeFence();

        return 0;
    }
}
