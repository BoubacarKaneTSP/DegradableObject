package eu.cloudbutton.dobj.key;

import eu.cloudbutton.dobj.juc.ConcurrentHashMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class SimpleKeyGenerator implements KeyGenerator {

    private final int max_key_per_thread;

    public SimpleKeyGenerator() {
        this.max_key_per_thread = Integer.MAX_VALUE;
    }

    public SimpleKeyGenerator(int max_key_per_thread) {
        this.max_key_per_thread = max_key_per_thread;
    }

    @Override
    public Key nextKey() {
        long id = Math.abs(ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE));
        return new ThreadLocalKey(Thread.currentThread().getId(), id);
    }

    public Map<Integer, List<Key>> generateAndSplit(int n, int parallelism) {
        Map<Integer, List<Key>> keys = new ConcurrentHashMap<>();
        for (int i = 0; i < parallelism; i++) {
            keys.put(i, new ArrayList<>());
        }
        for (int i = 0; i < n; i++) {
            Key k = this.nextKey();
            int index = Math.abs(k.hashCode()% parallelism);
            keys.get(index).add(k);
        }
        return keys;
    }


}
