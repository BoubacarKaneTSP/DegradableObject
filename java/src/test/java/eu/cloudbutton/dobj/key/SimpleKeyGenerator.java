package eu.cloudbutton.dobj.key;

import java.util.concurrent.ThreadLocalRandom;

public class SimpleKeyGenerator implements KeyGenerator {

    private final ThreadLocalRandom random;

    public SimpleKeyGenerator() {
        this.random = ThreadLocalRandom.current();
    }

    @Override
    public long nextKey() {
        return random.nextLong();
    }

}
