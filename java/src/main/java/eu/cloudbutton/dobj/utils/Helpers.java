package eu.cloudbutton.dobj.utils;
import jdk.internal.misc.Unsafe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

import static java.lang.Thread.sleep;

public class Helpers {

    private static final Unsafe UNSAFE;
    private static final ExecutorService executor;
    private static final String prefix = "pool-1-thread-";

    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            UNSAFE = (Unsafe) f.get(null);
        } catch (Exception e) {
            throw new Error(e);
        }
        executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    public static final Unsafe getUNSAFE() {
        return UNSAFE;
    }

    public static final void executeAll(int parallelism, Callable<Void> callable) {
        try {
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

    public static ExecutorService getExecutor() {
        return executor;
    }

    public static final String getThreadNamePrefix() {
        return prefix;
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

    public static String getProcessId() {
        String processName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
        return processName.split("@")[0];
    }

    public static void performHeapDump(String tag, String when, int nbUser) {
        System.out.println("Performing heapDump");
        String jcmdCommand = "jcmd";
        String processId = getProcessId();

        try {
            Process process = Runtime.getRuntime().exec(jcmdCommand);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            while ((line = reader.readLine()) != null) {
                // Recherche de la ligne contenant le processus Java souhaité
                if (line.contains(processId)) {
                    String[] tokens = line.trim().split("\\s+");
                    String pid = tokens[0];
                    String heapDumpCommand = "jcmd " + pid + " GC.heap_dump " + "heapdump_"+ when +"_benchmark_" + tag +"_" + nbUser +".hprof";

                    // Exécution de la commande jcmd pour effectuer le heap dump
                    Process heapDumpProcess = Runtime.getRuntime().exec(heapDumpCommand);

                    // Attente de la fin de l'exécution de la commande
                    int exitCode = heapDumpProcess.waitFor();

                    if (exitCode == 0) {
                        System.out.println("Heap dump effectué avec succès !");
                    } else {
                        System.out.println("Erreur lors de l'exécution de la commande jcmd.");
                    }

                    break;
                }
            }

            reader.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public static String mapEntryToString(Object key, Object val) {
        final String k, v;
        final int klen, vlen;
        final char[] chars =
                new char[(klen = (k = objectToString(key)).length()) +
                        (vlen = (v = objectToString(val)).length()) + 1];
        k.getChars(0, klen, chars, 0);
        chars[klen] = '=';
        v.getChars(0, vlen, chars, klen + 1);
        return new String(chars);
    }

    public static String objectToString(Object x) {
        // Extreme compatibility with StringBuilder.append(null)
        String s;
        return (x == null || (s = x.toString()) == null) ? "null" : s;
    }
}
