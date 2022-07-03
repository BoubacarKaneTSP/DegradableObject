package eu.cloudbutton.dobj.Benchmark.Tester;

import eu.cloudbutton.dobj.Benchmark.Benchmark;
import eu.cloudbutton.dobj.map.CollisionKey;

import java.util.AbstractSet;

public class SetFiller extends Filler<AbstractSet>{

    public SetFiller(AbstractSet set, long nbOps) {
        super(set, nbOps);
    }

    @Override
    public void fill() {

        for (long i = 0; i < nbOps; i++) {
            if(Benchmark.collisionKey)
                object.add(new CollisionKey(Long.toString(i)));
            else
                object.add(Long.toString(i));
        }
    }
}
