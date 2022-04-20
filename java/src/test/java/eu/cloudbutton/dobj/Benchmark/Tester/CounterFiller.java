package eu.cloudbutton.dobj.Benchmark.Tester;

import eu.cloudbutton.dobj.Counter.AbstractCounter;

public class CounterFiller extends Filler<AbstractCounter> {

    public CounterFiller(AbstractCounter object, long nbOps) {
        super(object, nbOps);
    }

    @Override
    public void fill() {
        for (int i = 0; i < nbOps; i++) {
            object.incrementAndGet();
        }
    }
}
