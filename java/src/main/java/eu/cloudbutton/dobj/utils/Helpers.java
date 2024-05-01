package eu.cloudbutton.dobj.utils;
import jdk.internal.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Helpers {

    private static final Unsafe UNSAFE;

    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            UNSAFE = (Unsafe) f.get(null);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    public static Unsafe getUNSAFE() {
        return UNSAFE;
    }

    public static String toString(Object[] a, int size, int charLength) {
        // assert a != null;
        // assert size > 0;

        // Copy each string into a perfectly sized char[]
        // Length of [ , , , ] == 2 * size
        final char[] chars = new char[charLength + 2 * size];
        chars[0] = '[';
        int j = 1;
        for (int i = 0; i < size; i++) {
            if (i > 0) {
                chars[j++] = ',';
                chars[j++] = ' ';
            }
            String s = (String) a[i];
            int len = s.length();
            s.getChars(0, len, chars, j);
            j += len;
        }
        chars[j] = ']';
        // assert j == chars.length - 1;
        return new String(chars);
    }

    public static void executeAll(int parallelism, Callable<Void> callable) {
        try {
            ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
            List<Future<Void>> futures = new ArrayList<>();
                for (int i = 0; i < parallelism; i++) {
                    futures.add(executor.submit(callable));
                }
                for (Future<Void> future : futures) {
                        future.get();
                }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
