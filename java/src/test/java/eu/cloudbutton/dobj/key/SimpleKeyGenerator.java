package eu.cloudbutton.dobj.key;

import java.util.concurrent.ThreadLocalRandom;

public class SimpleKeyGenerator implements KeyGenerator {

    private int max_key_per_thread;

    public SimpleKeyGenerator(int max_key_per_thread) {
        this.max_key_per_thread = max_key_per_thread;
    }

    @Override
    public Key nextKey() {
        int nextInt = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE);
        long id = Math.max(nextInt, 0);
        return new ThreadLocalKey(Thread.currentThread().getId(), id, max_key_per_thread);
    }

}
