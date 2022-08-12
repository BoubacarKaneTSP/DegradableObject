package eu.cloudbutton.dobj.Benchmark.Tester;

import eu.cloudbutton.dobj.Noop;
import eu.cloudbutton.dobj.counter.AbstractCounter;

import java.util.*;

public class FactoryFiller {

    private final Object object;
    private final long nbOps;
    private boolean useCollisionKey;

    public FactoryFiller(Object object, long nbOps, boolean useCollisionKey) {
        this.object = object;
        this.nbOps = nbOps;
        this.useCollisionKey = useCollisionKey;
    }

    public Filler createFiller() throws ClassNotFoundException {

        if (object instanceof AbstractMap)
            return new MapFiller((AbstractMap) object, nbOps, useCollisionKey);
        else if (object instanceof AbstractSet)
            return new SetFiller((AbstractSet) object, nbOps, useCollisionKey);
        else if (object instanceof AbstractQueue)
            return new QueueFiller((AbstractQueue) object, nbOps);
        else if (object instanceof List)
            return new ListFiller((AbstractList) object, nbOps);
        else if (object instanceof AbstractCounter)
            return new CounterFiller((AbstractCounter) object, nbOps);
        else if (object instanceof Noop)
            return new NoopFiller((Noop) object, nbOps);
        else
            throw new ClassNotFoundException("The Filler for "+ object.getClass() +" may not exists");
    }
}
