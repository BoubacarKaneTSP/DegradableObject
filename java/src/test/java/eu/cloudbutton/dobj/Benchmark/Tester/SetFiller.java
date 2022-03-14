package eu.cloudbutton.dobj.Benchmark.Tester;

import java.util.AbstractSet;
import java.util.Random;

public class SetFiller extends Filler<AbstractSet>{

    public SetFiller(AbstractSet set, long nbOps) {
        super(set, nbOps);
    }

    @Override
    public void fill() {

//        Random random = new Random();

        for (long i = 0; i < nbOps; i++) {
            object.add(i);
        }
    }
}
