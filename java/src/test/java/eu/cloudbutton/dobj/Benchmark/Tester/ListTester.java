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

        int rand = random.nextInt(ITEM_PER_THREAD);

        switch (type) {
            case ADD:
                startTime = System.nanoTime();
                for (int i = 0; i < nbRepeat; i++) {
                    object.add(rand);
                }
                endTime = System.nanoTime();
                break;
            case REMOVE:
                startTime = System.nanoTime();
                for (int i = 0; i < nbRepeat; i++) {
                    object.remove(rand);
                }
                endTime = System.nanoTime();
                break;
            case READ:
                startTime = System.nanoTime();
                for (int i = 0; i < nbRepeat; i++) {
                    object.get(rand);
                }
                endTime = System.nanoTime();
                break;
        }

        return (endTime - startTime)/nbRepeat;
    }
}