package eu.cloudbutton.dobj.benchmark.tester;

import eu.cloudbutton.dobj.benchmark.Microbenchmark.opType;
import eu.cloudbutton.dobj.incrementonly.BoxedLong;
import eu.cloudbutton.dobj.key.Key;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;

public class QueueTester extends Tester<Queue> {

    private final List<Integer> listValues;

    public QueueTester(Queue list, int[] ratios, CountDownLatch latch) {
        super(list, ratios, latch);
        listValues = new ArrayList<>();
    }

    @Override
    protected long test(opType type) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        long startTime = 0L, endTime = 0L;

        if (listValues.isEmpty()){
            for (int i = 0; i < nbRepeat; i++)
                listValues.add(i);

        }

        switch (type) {
            case ADD:
                startTime = System.nanoTime();
                for (int i = 0; i < nbRepeat; i++)
                    object.offer(listValues.get(i));
                endTime = System.nanoTime();
                break;
            case REMOVE:
                startTime = System.nanoTime();
                for (int i = 0; i < nbRepeat; i++)
                    object.poll();
                endTime = System.nanoTime();
                break;
            case READ:
                startTime = System.nanoTime();
                for (int i = 0; i < nbRepeat; i++)
                    object.contains(listValues.get(i));
                endTime = System.nanoTime();
                break;
        }

        return (endTime - startTime);
    }

}
