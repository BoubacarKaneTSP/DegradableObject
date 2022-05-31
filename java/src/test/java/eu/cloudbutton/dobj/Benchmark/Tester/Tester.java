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

        int n, add = 0, remove = 0, read = 0, addFail = 0, removeFail = 0, readFail = 0;
        long timeAdd = 0, timeRemove = 0, timeRead = 0, elapsedTime;

        opType type;

        latch.countDown();

        try{
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

                if (Benchmark.ratioFail){
                    elapsedTime = test(type);
                }else{
                    long cumulTime = 0;
                    for (int i = 0; i < 200; i++) {
                        cumulTime += test(type);
                    }
                    elapsedTime = cumulTime / 200;
                }

                switch (type) {
                    case ADD:
                        if (elapsedTime != 0)
                            add++;
                        else
                            addFail++;
                        timeAdd += elapsedTime;
                        break;
                    case REMOVE:
                        if (elapsedTime != 0)
                            remove++;
                        else
                            removeFail++;
                        timeRemove += elapsedTime;
                        break;
                    case READ:
                        if (elapsedTime != 0)
                            read++;
                        else
                            readFail++;
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

        Benchmark.nbAddFail.addAndGet(addFail);
        Benchmark.nbRemoveFail.addAndGet(removeFail);
        Benchmark.nbReadFail.addAndGet(readFail);

        Benchmark.timeAdd.addAndGet(timeAdd);
        Benchmark.timeRemove.addAndGet(timeRemove);
        Benchmark.timeRead.addAndGet(timeRead);

        return null;
    }

    protected abstract long test(opType type);
}
