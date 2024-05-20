package eu.cloudbutton.dobj.key;

import eu.cloudbutton.dobj.juc.ConcurrentHashMap;
import eu.cloudbutton.dobj.juc.ThreadLocalRandom;
import jdk.internal.misc.CarrierThreadLocal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class SimpleKeyGenerator implements KeyGenerator {

    private ThreadLocalRandom random;

    public SimpleKeyGenerator() {
        random = ThreadLocalRandom.current();
        random.setSeed(0); // to improve performance reproducibility.
    }

    @Override
    public Key nextKey() {
        return new SimpleKey(random.nextLong());
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
