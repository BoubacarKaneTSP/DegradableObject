package eu.cloudbutton.dobj.Benchmark.Tester;

import eu.cloudbutton.dobj.map.CollisionKey;

import java.lang.instrument.Instrumentation;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static eu.cloudbutton.dobj.Benchmark.Benchmark.collisionKey;

public class MapFiller extends Filler<AbstractMap> {

    public MapFiller(AbstractMap map, long nbOps) {
        super(map, nbOps);
    }

    @Override
    public void fill() {

        Random random = new Random();

        List<CollisionKey> collisionKeyList = new ArrayList<>();

        if (collisionKey) {
            for (int i = 0; i < nbOps; i++) {
                collisionKeyList.add(new CollisionKey(Integer.toString(random.nextInt())));
            }

            System.out.println("Done creating colkey objects");

            for (int i = 0; i < nbOps; i++) {
                object.put(collisionKeyList.get(i), i);
            }
        }else{
            for (int i = 0; i < nbOps; i++) {
                object.put(Integer.toString(i), i);
            }
        }

    }
}
