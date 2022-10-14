package eu.cloudbutton.dobj.benchmark.tester;

import eu.cloudbutton.dobj.incrementonly.BoxedLong;
import eu.cloudbutton.dobj.map.CollisionKeyFactory;
import eu.cloudbutton.dobj.map.PowerLawCollisionKey;
import eu.cloudbutton.dobj.benchmark.Microbenchmark.opType;
import java.lang.reflect.InvocationTargetException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class SetTester extends Tester<Set> {

    private boolean useCollisionKey;

    public SetTester(Set set, int[] ratios, CountDownLatch latch, boolean useCollisionKey) {
        super(set, ratios, latch);
        this.useCollisionKey = useCollisionKey;
    }

    @Override
    protected long test(opType type) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

        long startTime = 0L, endTime = 0L;

        int val = 0;

        AbstractList list = new ArrayList<>();

        CollisionKeyFactory factory = null;

        if (useCollisionKey){
            factory = new CollisionKeyFactory();
            factory.setFactoryCollisionKey(PowerLawCollisionKey.class);
        }

        for (int i = 0; i < nbRepeat; i++) {
            int rand = random.nextInt(ITEM_PER_THREAD);
            String iid = Thread.currentThread().getId()  + Long.toString(rand);

            if (useCollisionKey)
                list.add(factory.getCollisionKey());
            else
                list.add(iid);
        }

        switch (type) {
            case ADD:
                startTime = System.nanoTime();
                for (int i = 0; i < nbRepeat; i++) {
                    object.add(list.get(i));
                }
                endTime = System.nanoTime();
                break;
            case REMOVE:
                if(Thread.currentThread().getName().contains("thread-1")){
                    startTime = System.nanoTime();
                    for (int i = 0; i < nbRepeat; i++) {
                        object.remove(list.get(i));
                    }
                    endTime = System.nanoTime();
                }
                break;
            case READ:
                if(Thread.currentThread().getName().contains("thread-1")) {
                    startTime = System.nanoTime();
                    for (int i = 0; i < nbRepeat; i++) {
                        for (Object o : object) {
                            val += 1;
                        }
//                    object.contains(list.get(i));
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
