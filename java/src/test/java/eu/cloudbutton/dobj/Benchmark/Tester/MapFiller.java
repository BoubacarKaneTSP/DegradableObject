package eu.cloudbutton.dobj.Benchmark.Tester;

import eu.cloudbutton.dobj.map.CollisionKey;

import java.util.AbstractMap;
import java.util.Random;

public class MapFiller extends Filler<AbstractMap> {

    public MapFiller(AbstractMap map, long nbOps) {
        super(map, nbOps);
    }

    @Override
    public void fill() {

        Random random = new Random();

        for (int i = 0; i < nbOps; i++) {
            CollisionKey collisionKey = new CollisionKey(Integer.toString(random.nextInt()));
            object.put(collisionKey, Integer.toString(i));
        }
    }
}
