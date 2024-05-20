package eu.cloudbutton.dobj.unit;

import eu.cloudbutton.dobj.key.SimpleKeyGenerator;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.Arrays;

public class CollisionTest {

    private static final int HASH_BITS = 0x7fffffff;
    private static final int MAXIMUM_CAPACITY = 1 << 30;

    private SimpleKeyGenerator generator;

    private int[] ITEMS = {10_000, 100_000, 1_000_000};

    @BeforeTest
    void setUp() {
        generator = new SimpleKeyGenerator();
    }

    @Test
    public void testCollision() {
        System.out.println("size;table_size;collision_rate;average_chain_size;max_chain_size");
        Arrays.stream(ITEMS).forEach(i-> doTest(i));
    }

    public void doTest(int k) {
        int tableSize = tableSizeFor(k);
        int[] hashes = new int[tableSize];
        for (int i = 0; i < k; i++) {
            hashes[spread(generator.nextKey().hashCode()) % tableSize] ++;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(k).append(";");
        stringBuilder.append(tableSizeFor(tableSize)).append(";");
        long collision = Arrays.stream(hashes).filter(i -> i>1).count();
        stringBuilder.append((double) collision / tableSize).append(";");
        stringBuilder.append(Arrays.stream(hashes).filter(i -> i>1).average().getAsDouble()).append(";");
        stringBuilder.append(Arrays.stream(hashes).filter(i -> i>1).max().getAsInt()).append(";");
        System.out.println(stringBuilder);
    }

    static final int spread(int h) {
        return (h ^ (h >>> 16)) & HASH_BITS;
    }

    static final int tableSizeFor(int c) {
        int n = -1 >>> Integer.numberOfLeadingZeros(c - 1);
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }
}
