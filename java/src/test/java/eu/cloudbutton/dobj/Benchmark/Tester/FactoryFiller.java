package eu.cloudbutton.dobj.Benchmark.Tester;

import eu.cloudbutton.dobj.counter.AbstractCounter;

import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.AbstractQueue;
import java.util.AbstractSet;

public class FactoryFiller {

    private final Object object;
    private final long nbOps;

    public FactoryFiller(Object object, long nbOps) {
        this.object = object;
        this.nbOps = nbOps;
    }

    public Filler createFiller() throws ClassNotFoundException {

        if (object instanceof AbstractMap)
            return new MapFiller((AbstractMap) object, nbOps);
        else if (object instanceof AbstractSet)
            return new SetFiller((AbstractSet) object, nbOps);
        else if (object instanceof AbstractQueue)
            return new QueueFiller((AbstractQueue) object, nbOps);
        else if (object instanceof AbstractList)
            return new ListFiller((AbstractList) object, nbOps);
        else if (object instanceof AbstractCounter)
            return new CounterFiller((AbstractCounter) object, nbOps);
        else
            throw new ClassNotFoundException("The Filler for "+ object.getClass() +" may not exists");
    }
}
