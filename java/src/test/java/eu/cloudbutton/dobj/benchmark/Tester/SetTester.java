package eu.cloudbutton.dobj.benchmark.Tester;

import eu.cloudbutton.dobj.map.CollisionKeyFactory;
import eu.cloudbutton.dobj.map.PowerLawCollisionKey;

import java.lang.reflect.InvocationTargetException;
import java.util.AbstractList;
import java.util.AbstractSet;
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
                System.out.println("A");
                startTime = System.nanoTime();
                for (int i = 0; i < nbRepeat; i++) {
                    object.add(list.get(i));
                }
                endTime = System.nanoTime();
                break;
            case REMOVE:
                System.out.println("rem");
                if(Thread.currentThread().getName().contains("thread-1")){
                    startTime = System.nanoTime();
                    for (int i = 0; i < nbRepeat; i++) {
                        object.remove(list.get(i));
                    }
                    endTime = System.nanoTime();
                }
                break;
            case READ:
                startTime = System.nanoTime();
                for (int i = 0; i < nbRepeat; i++) {
                    System.out.println("L");
                    for (Object o : object){
                    }
//                    object.contains(list.get(i));
                }
                endTime = System.nanoTime();
                break;
        }
        System.out.println("O");

        return (endTime - startTime);
    }
}
