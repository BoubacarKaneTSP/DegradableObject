package eu.cloudbutton.dobj.Benchmark.Tester;

import eu.cloudbutton.dobj.Benchmark.Benchmark;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

public abstract class Tester<T> implements Callable<Void> {

    protected static final int ITEM_PER_THREAD = 1000;
    protected final ThreadLocalRandom random;
    protected final T object;
    protected final int[] ratios;
    protected final CountDownLatch latch;
    protected final long nbOps;

    public Tester(T object, int[] ratios, CountDownLatch latch, long nbOps) {
        this.random = ThreadLocalRandom.current();
        this.object = object;
        this.ratios = ratios;
        this.latch = latch;
        this.nbOps = nbOps;
    }

    @Override
    public Void call() {

        latch.countDown();
        long startTime, endTime;

        int n, add = 0, remove = 0, read = 0;
        char type;

        try{
            latch.await();

            // warm up
            while (Benchmark.flag.get()) {

                n = this.random.nextInt(101);

                if (n <= ratios[0]) {
                    if (n % 2 == 0)
                        type = 'a';
                    else
                        type = 'r';
                } else {
                    type = 'c';
                }

                test(type);
            }

            // compute
            while (!Benchmark.flag.get()) {
                n = this.random.nextInt(101);

                if (n < ratios[0]) {
                    if (n % 2 == 0)
                        type = 'a';
                    else
                        type = 'r';
                } else {
                    type = 'c';
                }

                startTime = System.nanoTime();
                test(type);
                endTime = System.nanoTime();

                switch (type) {
                    case 'a':
                        add++;
                        Benchmark.timeAdd.addAndGet(endTime - startTime);
                        break;
                    case 'r':
                        remove++;
                        Benchmark.timeRemove.addAndGet(endTime - startTime);
                        break;
                    case 'c':
                        read++;
                        Benchmark.timeRead.addAndGet(endTime - startTime);
                        break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        Benchmark.nbAdd.increment(add);
        Benchmark.nbRemove.increment(remove);
        Benchmark.nbRead.increment(read);

        return null;
    }

    protected abstract void test(char type);
}
