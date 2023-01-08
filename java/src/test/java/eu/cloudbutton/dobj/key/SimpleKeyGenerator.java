package eu.cloudbutton.dobj.key;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class SimpleKeyGenerator implements KeyGenerator {

    private ThreadLocal<Random> random;
    private int max_key_per_thread;

    public SimpleKeyGenerator(int max_key_per_thread) {
        System.out.println("BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB");
        random = ThreadLocal.withInitial(() -> new Random(System.nanoTime()+Thread.currentThread().getId()));
        this.max_key_per_thread = max_key_per_thread;
    }

    @Override
    public Key nextKey() {
        int nextInt = random.get().nextInt(Integer.MAX_VALUE);
        long id = nextInt < 0 ? 0 : nextInt;
        return new ThreadLocalKey(Thread.currentThread().getId(), id, max_key_per_thread);
    }

}
