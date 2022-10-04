package eu.cloudbutton.dobj.map;

import nl.peterbloem.powerlaws.DiscreteApproximate;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class CollisionKeyFactory {

    List<Integer> listHashCode = new ArrayList<>();
    protected final ThreadLocalRandom random;
    int bound = 1000;

    private Constructor<? extends AbstractCollisionKey> constructorCollisionKey;

    public CollisionKeyFactory() {
        this.random = ThreadLocalRandom.current();
        fillListHashCode();
    }

    public void setFactoryCollisionKey(Class<? extends AbstractCollisionKey> collisionKeyClass) throws NoSuchMethodException {
        constructorCollisionKey = collisionKeyClass.getConstructor();
    }

    public CollisionKey getCollisionKey() throws InvocationTargetException, InstantiationException, IllegalAccessException {

        CollisionKey collisionKey = constructorCollisionKey.newInstance();

        if (collisionKey instanceof PowerLawCollisionKey)
            collisionKey.setHash(listHashCode.get(random.nextInt(bound)));

        return collisionKey;
    }

    private void fillListHashCode(){

        List<Integer> data = new DiscreteApproximate(1, 1.39).generate(bound);
        int i = 0;

        int nbUsers = 1000000;
        double ratio = 100000 / 175000000.0; //10⁵ is ~ the number of follow max on twitter and 175_000_000 is the number of user on twitter (stats from the article)
        long max = (long) ((long) nbUsers * ratio);

//        max = 10;

        for (int val: data){
            if (val >= max) {
                data.set(i, (int) max);
            }
            if (val < 0)
                data.set(i, 0);
            i++;
        }

        for (int j = 0; j < bound; j++) {
            for (int k = 0; k < data.get(j); k++) {
                listHashCode.add(j);
            }
        }
    }
}
