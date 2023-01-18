package eu.cloudbutton.dobj.benchmark.tester;

import eu.cloudbutton.dobj.Noop;
import eu.cloudbutton.dobj.incrementonly.Counter;
import eu.cloudbutton.dobj.key.Key;
import eu.cloudbutton.dobj.key.KeyGenerator;
import eu.cloudbutton.dobj.key.RetwisKeyGenerator;
import eu.cloudbutton.dobj.key.SimpleKeyGenerator;
import eu.cloudbutton.dobj.queue.WaitFreeQueue;
import eu.cloudbutton.dobj.register.AtomicWriteOnceReference;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class FactoryFiller {

    private final Object object;
    private final long nbOps;
    private KeyGenerator keyGenerator;

    public FactoryFiller(Object object, long nbOps, boolean useCollisionKey, int max_item_per_thread) {
        this.object = object;
        this.nbOps = nbOps;
        keyGenerator = useCollisionKey ? new RetwisKeyGenerator(max_item_per_thread) : new SimpleKeyGenerator(max_item_per_thread);
    }

    public Filler createFiller() throws ClassNotFoundException {

        if (object instanceof Map)
            return new Filler<>((Map) object, keyGenerator, nbOps) {
                @Override
                public void doFill(Key key) {
                    object.put(key,key);
                }
            };
        else if (object instanceof Set)
            return new Filler<>((Set) object, keyGenerator, nbOps) {
                @Override
                public void doFill(Key key) {
                    object.add(key);
                }
            };
        else if (object instanceof WaitFreeQueue)
            return new Filler<>((WaitFreeQueue) object, keyGenerator, nbOps) {
                WaitFreeQueue.Handle<Integer> h = object.register();
                @Override
                public void doFill(Key key) {
                    object.enqueue(key, h);
                }
            };
        else if (object instanceof Queue)
            return new Filler<>((Queue) object, keyGenerator, nbOps) {
                @Override
                public void doFill(Key key) {
                    object.offer(key);
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
        else if (object instanceof AtomicWriteOnceReference){
            return new Filler<>((AtomicWriteOnceReference) object, keyGenerator, 1) {
                @Override
                public void doFill(Key key) {
                    object.set(1);
                }
            };
        }
        else if (object instanceof AtomicReference){
            return new Filler<>((AtomicReference) object, keyGenerator, 1) {
                @Override
                public void doFill(Key key) {
                    object.set(1);
                }
            };
        }
        else
            throw new ClassNotFoundException("The Filler for "+ object.getClass() +" may not exists");
    }
}
