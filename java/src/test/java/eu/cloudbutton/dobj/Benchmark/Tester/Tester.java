package eu.cloudbutton.dobj.Benchmark.Tester;

import eu.cloudbutton.dobj.Benchmark.Benchmark;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

public abstract class Tester<T> implements Callable<Void> {

    enum opType{
        ADD,
        REMOVE,
        READ
    }

    protected static final int ITEM_PER_THREAD = 1000;
    protected final ThreadLocalRandom random;
    protected final T object;
    protected final int[] ratios;
    protected final CountDownLatch latch;

    public Tester(T object, int[] ratios, CountDownLatch latch) {
        this.random = ThreadLocalRandom.current();
        this.object = object;
        this.ratios = ratios;
        this.latch = latch;
    }

    @Override
    public Void call() {

        long startTime, endTime;

        int n, add = 0, remove = 0, read = 0;
        long timeAdd = 0, timeRemove = 0, timeRead = 0;

        opType type;

        latch.countDown();

        try{
            latch.await();

            // TRY WITH ONLY ONE RANDOM NUMBER AND DO % 100 FOR THE OTHER

            // warm up
            while (Benchmark.flag.get()) {

                n = this.random.nextInt(100);

                if (n < ratios[0]) {
                    if (n % 2 == 0)
                        type = opType.ADD;
                    else
                        type = opType.REMOVE;
                } else {
                    type = opType.READ;
                }

                int rand = random.nextInt(ITEM_PER_THREAD);
                long iid = Thread.currentThread().getId() * 1000000000L + rand;
                test(type, iid);
            }

            // compute
            while (!Benchmark.flag.get()) {
                n = this.random.nextInt(100);

                if (n < ratios[0]) {
                    if (n % 2 == 0)
                        type = opType.ADD;
                    else
                        type = opType.REMOVE;
                } else {
                    type = opType.READ;
                }

                int rand = random.nextInt(ITEM_PER_THREAD);
                long iid = Thread.currentThread().getId() * 1_000_000_000L + rand;

                startTime = System.nanoTime();
                test(type, iid);
                endTime = System.nanoTime();

                switch (type) {
                    case ADD:
                        add++;
                        timeAdd += endTime - startTime;
                        break;
                    case REMOVE:
                        remove++;
                        timeRemove += endTime - startTime;
                        break;
                    case READ:
                        read++;
                        timeRead += endTime - startTime;
                        break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        Benchmark.nbAdd.addAndGet(add);
        Benchmark.nbRemove.addAndGet(remove);
        Benchmark.nbRead.addAndGet(read);

        Benchmark.timeAdd.addAndGet(timeAdd);
        Benchmark.timeRemove.addAndGet(timeRemove);
        Benchmark.timeRead.addAndGet(timeRead);

        return null;
    }

    protected abstract void test(opType type, long iid);
}
