package eu.cloudbutton.dobj.key;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class SimpleKeyGenerator implements KeyGenerator {

    private int max_key_per_thread;

    public SimpleKeyGenerator(int max_key_per_thread) {
        this.max_key_per_thread = max_key_per_thread;
    }

    @Override
    public Key nextKey() {
        long id = Math.abs(ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE));
        return new ThreadLocalKey(Thread.currentThread().getId(), id, max_key_per_thread);
    }

}
