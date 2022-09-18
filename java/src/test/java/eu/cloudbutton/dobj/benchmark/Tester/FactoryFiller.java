package eu.cloudbutton.dobj.benchmark.Tester;

import eu.cloudbutton.dobj.Noop;
import eu.cloudbutton.dobj.incrementonly.Counter;

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

        if (object instanceof Map)
            return new MapFiller((Map) object, nbOps, useCollisionKey);
        else if (object instanceof Set)
            return new SetFiller((Set) object, nbOps, useCollisionKey);
        else if (object instanceof AbstractQueue)
            return new QueueFiller((Queue) object, nbOps);
        else if (object instanceof List)
            return new ListFiller((AbstractList) object, nbOps);
        else if (object instanceof Counter)
            return new CounterFiller((Counter) object, nbOps);
        else if (object instanceof Noop)
            return new NoopFiller((Noop) object, nbOps);
        else
            throw new ClassNotFoundException("The Filler for "+ object.getClass() +" may not exists");
    }
}
