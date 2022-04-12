package eu.cloudbutton.dobj.Benchmark.Tester;

import java.util.AbstractList;

public class ListFiller extends Filler<AbstractList> {

    public ListFiller(AbstractList object, long nbOps) {
        super(object, nbOps);
    }

    @Override
    public void fill() {

        for (long i = 0; i < nbOps; i++) {
            object.add(i);
        }

    }
}