package eu.cloudbutton.dobj.benchmark.tester;

import eu.cloudbutton.dobj.incrementonly.BoxedLong;
import eu.cloudbutton.dobj.key.KeyGenerator;
import eu.cloudbutton.dobj.key.RetwisKeyGenerator;
import eu.cloudbutton.dobj.benchmark.Microbenchmark.opType;
import eu.cloudbutton.dobj.key.SimpleKeyGenerator;

import java.lang.reflect.InvocationTargetException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class SetTester extends Tester<Set> {

    private KeyGenerator keyGenerator;

    public SetTester(Set set, int[] ratios, CountDownLatch latch, boolean useCollisionKey) {
        super(set, ratios, latch);
        keyGenerator = useCollisionKey ? new RetwisKeyGenerator() : new SimpleKeyGenerator();
    }

    @Override
    protected long test(opType type) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

        long startTime = 0L, endTime = 0L;

        List list = new ArrayList<>();
        for (int i = 0; i < nbRepeat; i++) {
            list.add(keyGenerator.nextKey());
        }

        switch (type) {
            case ADD:
                startTime = System.nanoTime();
                for (int i = 0; i < nbRepeat; i++) {
//                    object.add(list.get(i));
                    object.add(i);
                }
                endTime = System.nanoTime();
                break;
            case REMOVE:
                startTime = System.nanoTime();
                for (int i = 0; i < nbRepeat; i++) {
//                    object.remove(list.get(i));
                    object.remove(i);
                }
                endTime = System.nanoTime();

                break;
            case READ:
                if(Thread.currentThread().getName().contains("thread-1")) {
                    int v = 0;
                    startTime = System.nanoTime();
                    for (int i = 0; i < nbRepeat; i++) {
                        for (Object o : object) {
                            v++;
                        }
                    }
                    endTime = System.nanoTime();
                }
                break;
        }

        return (endTime - startTime);
    }

    @Override
    protected long test(opType type, BoxedLong boxedLong) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        return 0;
    }
}
