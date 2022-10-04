package eu.cloudbutton.dobj.benchmark.Tester;

import eu.cloudbutton.dobj.benchmark.Microbenchmark;
import eu.cloudbutton.dobj.benchmark.Microbenchmark.opType;
import eu.cloudbutton.dobj.incrementonly.BoxedLong;

import java.lang.reflect.InvocationTargetException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
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
    protected int nbRepeat;

    public Tester(T object, int[] ratios, CountDownLatch latch) {
        this.random = ThreadLocalRandom.current();
        this.object = object;
        this.ratios = ratios;
        this.latch = latch;
        this.nbRepeat = 1000;
    }

    @Override
    public Void call() {

        int n, add = 0, remove = 0, read = 0, addFail = 0, removeFail = 0, readFail = 0;
        long timeAdd = 0, timeRemove = 0, timeRead = 0, elapsedTime;

        opType type;

        latch.countDown();

        try{
            // warm up
            while (Microbenchmark.flag.get()) {

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
            while (!Microbenchmark.flag.get()) {
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
                        if (elapsedTime != 0)
                            add+= nbRepeat;
                        else
                            addFail++;
                        timeAdd += elapsedTime;
                        break;
                    case REMOVE:
                        if (elapsedTime != 0)
                            remove+=nbRepeat;
                        else
                            removeFail++;
                        timeRemove += elapsedTime;
                        break;
                    case READ:
                        if (elapsedTime != 0)
                            read+=nbRepeat;
                        else
                            readFail++;
                        timeRead += elapsedTime;
                        break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        Microbenchmark.nbAdd.addAndGet(add);
        Microbenchmark.nbRemove.addAndGet(remove);
        Microbenchmark.nbRead.addAndGet(read);

        Microbenchmark.nbAddFail.addAndGet(addFail);
        Microbenchmark.nbRemoveFail.addAndGet(removeFail);
        Microbenchmark.nbReadFail.addAndGet(readFail);

        Microbenchmark.timeAdd.addAndGet(timeAdd);
        Microbenchmark.timeRemove.addAndGet(timeRemove);
        Microbenchmark.timeRead.addAndGet(timeRead);

        return null;
    }

    protected abstract long test(opType type) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException;
}

