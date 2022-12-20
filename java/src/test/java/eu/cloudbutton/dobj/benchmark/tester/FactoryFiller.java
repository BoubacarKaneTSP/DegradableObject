package eu.cloudbutton.dobj.benchmark.tester;

import eu.cloudbutton.dobj.Noop;
import eu.cloudbutton.dobj.incrementonly.Counter;
import eu.cloudbutton.dobj.key.Key;
import eu.cloudbutton.dobj.key.KeyGenerator;
import eu.cloudbutton.dobj.key.RetwisKeyGenerator;
import eu.cloudbutton.dobj.key.SimpleKeyGenerator;

import java.util.*;

public class FactoryFiller {

    private final Object object;
    private final long nbOps;
    private KeyGenerator keyGenerator;

    public FactoryFiller(Object object, long nbOps, boolean useCollisionKey) {
        this.object = object;
        this.nbOps = nbOps;
        keyGenerator = useCollisionKey ? new RetwisKeyGenerator() : new SimpleKeyGenerator();
    }

    public Filler createFiller() throws ClassNotFoundException {

        if (object instanceof Map)
            return new Filler<>((Map) object, keyGenerator, nbOps) {
                @Override
                public void doFill(Key key) {
                    object.put(key,key);
                }
            };
        else if (object instanceof Collection)
            return new Filler<>((Set) object, keyGenerator, nbOps) {
                @Override
                public void doFill(Key key) {
                    object.add(key);
                }
            };
        else if (object instanceof Counter)
            return new Filler<>((Counter) object, keyGenerator, nbOps) {
                @Override
                public void doFill(Key key) {
                    object.incrementAndGet();
                }
            };
        else if (object instanceof Noop)
            return new Filler<>(object, keyGenerator, nbOps) {
                @Override
                public void doFill(Key key) {
                    // no-op
                }
            };
        else
            throw new ClassNotFoundException("The Filler for "+ object.getClass() +" may not exists");
    }
}
