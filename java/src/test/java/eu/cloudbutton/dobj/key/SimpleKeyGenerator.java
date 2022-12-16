package eu.cloudbutton.dobj.key;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class SimpleKeyGenerator implements KeyGenerator {

    private final ThreadLocal<Random> random; // to avoid collisions

    public SimpleKeyGenerator() {
        this.random = ThreadLocal.withInitial(() -> {return new Random(System.nanoTime()+Thread.currentThread().getId());});
    }

    @Override
    public long nextKey() {
        return random.get().nextLong();
    }

}
