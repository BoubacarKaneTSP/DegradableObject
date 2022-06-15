package eu.cloudbutton.dobj.Benchmark.Tester;

import eu.cloudbutton.dobj.Noop;

public class NoopFiller extends Filler<Noop> {

    public NoopFiller(Noop object, long nbOps) {
        super(object, nbOps);
    }

    @Override
    public void fill() {
    }
}
