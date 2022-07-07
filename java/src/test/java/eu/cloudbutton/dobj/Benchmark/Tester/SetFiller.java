package eu.cloudbutton.dobj.Benchmark.Tester;

import eu.cloudbutton.dobj.map.CollisionKeyFactory;
import eu.cloudbutton.dobj.map.PowerLawCollisionKey;

import java.lang.reflect.InvocationTargetException;
import java.util.AbstractSet;

public class SetFiller extends Filler<AbstractSet>{

    private boolean useCollisionKey;

    public SetFiller(AbstractSet set, long nbOps, boolean useCollisionKey) {
        super(set, nbOps);
        this.useCollisionKey = useCollisionKey;
    }

    @Override
    public void fill() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

        CollisionKeyFactory factory = null;

        if (useCollisionKey){
            factory = new CollisionKeyFactory();
            factory.setFactoryCollisionKey(PowerLawCollisionKey.class);
        }

        for (long i = 0; i < nbOps; i++) {
            if(useCollisionKey)
                object.add(factory.getCollisionKey());
            else
                object.add(Long.toString(i));
        }
    }
}
