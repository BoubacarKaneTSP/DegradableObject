package eu.cloudbutton.dobj.Benchmark.Tester;

import java.util.*;
import java.util.concurrent.CountDownLatch;

public class ListTester extends Tester<AbstractList> {

    public ListTester(AbstractList list, int[] ratios, CountDownLatch latch) {
        super(list, ratios, latch);
    }

    @Override
    protected long test(opType type) {

        long startTime = 0L, endTime = 0L;

        AbstractList<Long> list = new ArrayList();

        for (int i = 0; i < nbRepeat; i++) {

            int rand = random.nextInt(ITEM_PER_THREAD);
            long iid = Thread.currentThread().getId() * 1_000_000_000L + rand;
            list.add(iid);
        }

        switch (type) {
            case ADD:
                startTime = System.nanoTime();
                for (int i = 0; i < nbRepeat; i++) {
                    object.add(list.get(i));
                }
                endTime = System.nanoTime();

                for (int i = 0; i < nbRepeat; i++) {
                    object.remove(list.get(i));
                }

                break;
            case REMOVE:
                startTime = System.nanoTime();
                for (int i = 0; i < nbRepeat; i++) {
                    object.remove(list.get(i));
                }
                endTime = System.nanoTime();
                break;
            case READ:
                startTime = System.nanoTime();
                for (int i = 0; i < nbRepeat; i++) {
                    object.contains(list.get(i));
                }
                endTime = System.nanoTime();
                break;
        }

        return (endTime - startTime)/nbRepeat;
    }
}