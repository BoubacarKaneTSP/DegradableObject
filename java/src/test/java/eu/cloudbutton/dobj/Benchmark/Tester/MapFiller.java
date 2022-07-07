package eu.cloudbutton.dobj.Benchmark.Tester;

import eu.cloudbutton.dobj.map.CollisionKeyFactory;
import eu.cloudbutton.dobj.map.PowerLawCollisionKey;

import java.lang.reflect.InvocationTargetException;
import java.util.AbstractMap;

public class MapFiller extends Filler<AbstractMap> {

    private boolean useCollisionKey;

    public MapFiller(AbstractMap map, long nbOps, boolean useCollisionKey) {
        super(map, nbOps);
        this.useCollisionKey = useCollisionKey;
    }

    @Override
    public void fill() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

        CollisionKeyFactory factory = new CollisionKeyFactory();

        factory.setFactoryCollisionKey(PowerLawCollisionKey.class);

        for (int i = 0; i < nbOps; i++) {
            if (useCollisionKey)
                object.put(factory.getCollisionKey(), i);
            else
                object.put(Integer.toString(i), i);
        }

    }
}
