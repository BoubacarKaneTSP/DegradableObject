package eu.cloudbutton.dobj.key;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class SimpleKeyGenerator implements KeyGenerator {

    private ThreadLocal<Random> random;

    public SimpleKeyGenerator() {
        random = ThreadLocal.withInitial(() -> new Random(System.nanoTime()+Thread.currentThread().getId()));
    }

    @Override
    public Key nextKey() {
        return new ThreadLocalKey(Thread.currentThread().getId(),random.get().nextLong());
    }

}
