package eu.cloudbutton.dobj.benchmark.tester;

import eu.cloudbutton.dobj.Noop;

public class NoopFiller extends Filler<Noop> {

    public NoopFiller(Noop object, long nbOps) {
        super(object, nbOps);
    }

    @Override
    public void fill() {
    }
}
