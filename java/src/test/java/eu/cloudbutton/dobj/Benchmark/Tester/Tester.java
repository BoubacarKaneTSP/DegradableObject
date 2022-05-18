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

        int n, add = 0, remove = 0, read = 0;
        long timeAdd = 0, timeRemove = 0, timeRead = 0, elapsedTime;

        opType type;

        latch.countDown();

        try{
            // TRY WITH ONLY ONE RANDOM NUMBER AND DO % 100 FOR THE OTHER

            // warm up
            while (Benchmark.flag.get()) {

                n = this.random.nextInt(100);

                if (n < ratios[0]) {
                    type = opType.ADD;
                }else if(n < ratios[0] + ratios[1]) {
                    type = opType.REMOVE;
                }else {
                    type = opType.READ;
                }

                test(type);
            }

            latch.await();

            // compute
            while (!Benchmark.flag.get()) {
                n = this.random.nextInt(100);

                if (n < ratios[0]) {
                    type = opType.ADD;
                }else if(n < ratios[0] + ratios[1]) {
                    type = opType.REMOVE;
                }else {
                    type = opType.READ;
                }

                elapsedTime = test(type);

                switch (type) {
                    case ADD:
                        add++;
                        timeAdd += elapsedTime;
                        break;
                    case REMOVE:
                        remove++;
                        timeRemove += elapsedTime;
                        break;
                    case READ:
                        read++;
                        timeRead += elapsedTime;
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

    protected abstract long test(opType type);
}
