package eu.cloudbutton.dobj.benchmark.Tester;

import eu.cloudbutton.dobj.benchmark.Microbenchmark.opType;

import java.util.Queue;
import java.util.concurrent.CountDownLatch;

public class QueueTester extends Tester<Queue> {

    public QueueTester(Queue list, int[] ratios, CountDownLatch latch) {
        super(list, ratios, latch);
    }

    @Override
    protected long test(opType type) {

        long startTime = 0L, endTime = 0L, val = 0;

        int rand = random.nextInt(Integer.MAX_VALUE);

        switch (type) {
            case ADD:
                startTime = System.nanoTime();
                for (int i = 0; i < nbRepeat; i++) {
                    object.offer(rand);
                }
                endTime = System.nanoTime();
                break;
            case REMOVE:
                ThreadLocal<Long> threadLocal ;
                startTime = System.nanoTime();
                for (int i = 0; i < nbRepeat; i++) {
                    object.poll();
                }
/*                Object obj ;
                do {
                    val += 1;
                    obj = object.poll();
                }while (obj != null);*/

                endTime = System.nanoTime();
                break;
            case READ:
                /*startTime = System.nanoTime();
                for (int i = 0; i < nbRepeat; i++) {
                    object.contains(rand);
                }
                endTime = System.nanoTime();*/
                break;
        }

        return (endTime - startTime);
    }
}
