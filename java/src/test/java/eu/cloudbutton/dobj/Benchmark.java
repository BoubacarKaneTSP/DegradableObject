package eu.cloudbutton.dobj;

import eu.cloudbutton.dobj.types.AbstractCounter;
import eu.cloudbutton.dobj.types.Factory;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.StringArrayOptionHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.kohsuke.args4j.OptionHandlerFilter.ALL;

public class Benchmark {

    private static ConcurrentLinkedQueue<Long> nbOperations = new ConcurrentLinkedQueue<>();
    private static AtomicBoolean flag;
    @Option(name = "-type", required = true, usage = "type to test")
    private String type;
    @Option(name = "-ratios", required = true, handler = StringArrayOptionHandler.class, usage = "ratios")
    private String[] ratios;
    @Option(name = "-nbThreads", usage = "Number of threads")
    private int nbThreads = Runtime.getRuntime().availableProcessors() / 2;
    @Option(name = "-time", usage = "How long will the test last (seconds)")
    private int time = 300;
    @Option(name = "-wTime", usage = "How long we wait till the test start (seconds)")
    private int wTime = 0;
    @Option(name = "-nbOps", usage = "Number of operations between two time's read")
    private long nbOps = 100_000_000;
    @Option(name = "-nbTest", usage = "Number of test")
    private int nbTest = 1;

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        new Benchmark().doMain(args);
    }

    public void doMain(String[] args) throws InterruptedException, ExecutionException {
        CmdLineParser parser = new CmdLineParser(this);

        try{
            // parse the arguments.
            parser.parseArgument(args);

            // you can parse additional arguments if you want.
            // parser.parseArgument("more","args");

            // after parsing arguments, you should check
            // if enough arguments are given.
            if (args.length < 1)
                throw new CmdLineException(parser, "No argument is given");

        } catch (CmdLineException e) {
            // if there's a problem in the command line,
            // you'll get this exception. this will report
            // an error message.
            System.err.println(e.getMessage());
            System.err.println("java SampleMain [options...] arguments...");
            // print the list of available options
            parser.printUsage(System.err);
            System.err.println();

            // print option sample. This is useful some time
            System.err.println("  Example: java Benchmark" + parser.printExample(ALL));

            return;
        }

        try{

            Factory factory = new Factory();
            String constructor = "create" + type;

            for (int i = 1; i <= nbThreads; ) {
                for (int a = 0; a < nbTest; a++) {
                    Object object = Factory.class.getDeclaredMethod(constructor).invoke(factory);

                    List<Callable<Void>> callables = new ArrayList<>();
                    ExecutorService executor = Executors.newFixedThreadPool(i);

                    CountDownLatch latch = new CountDownLatch(i);
                    FactoryTester factoryTester = new FactoryTester(
                            object,
                            new int[] {100},
                            latch, nbOps / i);
                    for (int j = 0; j < i-1; j++) {
                        Method m = factoryTester.getClass().getDeclaredMethod("create" + type + "Tester");
                        Tester tester = (Tester) m.invoke(factoryTester);
                        callables.add(tester);
                    }

                    FactoryTester factoryT = new FactoryTester(
                            object,
                            Arrays.stream(ratios).mapToInt(Integer::parseInt).toArray(),
                            latch,
                            nbOps/i);

                    Method m1 = factoryT.getClass().getDeclaredMethod("create" + type + "Tester");
                    Tester t = (Tester) m1.invoke(factoryT);
                    callables.add(t);

                    ExecutorService executorService = Executors.newFixedThreadPool(1);
                    flag = new AtomicBoolean();
                    flag.set(true);
                    executorService.submit(new Coordinator());

                    List<Future<Void>> futures;

                    // launch computation
                    futures = executor.invokeAll(callables, time + wTime, TimeUnit.SECONDS);

                    try{
                        for (Future<Void> future : futures) {
                            future.get();
                        }
                    } catch (CancellationException e) {
                        //ignore
                    }
                    TimeUnit.SECONDS.sleep(5);
                    executor.shutdown();

                }
                Long sum = 0L;
                for (Long val : nbOperations) {
                    sum += val;
                }
                double avg_op = sum / i;
                System.out.println(i + " " + (time) / avg_op); // printing the avg time per op for i thread(s)
                nbOperations = new ConcurrentLinkedQueue<>();
                i = 2 * i;
                if(i==2)
                    i = nbThreads;
                if (i > nbThreads && i != 2 * nbThreads) {
                    i = nbThreads;
                }
            }

        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        System.exit(0);
    }

    public static class FactoryTester {

        private final Object object;
        private final int[] ratios;
        private final CountDownLatch latch;
        private final long nbOps;

        FactoryTester(Object object, int[] ratios, CountDownLatch latch, long nbOps) {
            this.object = object;
            this.ratios = ratios;
            this.latch = latch;
            this.nbOps = nbOps;
        }

        public CounterTester createCounterTester() {
            return new CounterTester((AbstractCounter) object, ratios, latch, nbOps);
        }

        public DegradableCounterTester createDegradableCounterTester() {
            return new DegradableCounterTester((AbstractCounter) object, ratios, latch, nbOps);
        }

        public CounterSnapshotTester createCounterSnapshotTester() {
            return new CounterSnapshotTester((AbstractCounter) object, ratios, latch, nbOps);
        }

        public CounterSnapshotV2Tester createCounterSnapshotV2Tester() {
            return new CounterSnapshotV2Tester((AbstractCounter) object, ratios, latch, nbOps);
        }

        public ListTester createListTester() {
            return new ListTester((eu.cloudbutton.dobj.types.AbstractList) object, ratios, latch, nbOps);
        }

        public DegradableListTester createDegradableListTester() {
            return new DegradableListTester((eu.cloudbutton.dobj.types.AbstractList) object, ratios, latch, nbOps);
        }

        public ListSnapshotTester createListSnapshotTester() {
            return new ListSnapshotTester((eu.cloudbutton.dobj.types.AbstractList) object, ratios, latch, nbOps);
        }

        public ListSnapshotV2Tester createListSnapshotV2Tester() {
            return new ListSnapshotV2Tester((eu.cloudbutton.dobj.types.AbstractList) object, ratios, latch, nbOps);
        }

        public LinkedListTester createLinkedListTester() {
            return new LinkedListTester((eu.cloudbutton.dobj.types.AbstractList) object, ratios, latch, nbOps);
        }

        public DegradableLinkedListTester createDegradableLinkedListTester() {
            return new DegradableLinkedListTester((eu.cloudbutton.dobj.types.AbstractList) object, ratios, latch, nbOps);
        }

        public LinkedListSnapshotTester createLinkedListSnapshotTester() {
            return new LinkedListSnapshotTester((eu.cloudbutton.dobj.types.AbstractList) object, ratios, latch, nbOps);
        }

        public LinkedListSnapshotV2Tester createLinkedListSnapshotV2Tester() {
            return new LinkedListSnapshotV2Tester((eu.cloudbutton.dobj.types.AbstractList) object, ratios, latch, nbOps);
        }

        public SetTester createSetTester() {
            return new SetTester((eu.cloudbutton.dobj.types.AbstractSet) object, ratios, latch, nbOps);
        }

        public DegradableSetTester createDegradableSetTester() {
            return new DegradableSetTester((eu.cloudbutton.dobj.types.AbstractSet) object, ratios, latch, nbOps);
        }

        public SetSnapshotTester createSetSnapshotTester() {
            return new SetSnapshotTester((eu.cloudbutton.dobj.types.AbstractSet) object, ratios, latch, nbOps);
        }

        public SetSnapshotV2Tester createSetSnapshotV2Tester() {
            return new SetSnapshotV2Tester((eu.cloudbutton.dobj.types.AbstractSet) object, ratios, latch, nbOps);
        }

        public SecondDegradableListTester createSecondDegradableListTester() {
            return new SecondDegradableListTester((eu.cloudbutton.dobj.types.AbstractList) object, ratios, latch, nbOps);
        }

        public ThirdDegradableListTester createThirdDegradableListTester() {
            return new ThirdDegradableListTester((eu.cloudbutton.dobj.types.AbstractList) object, ratios, latch, nbOps);
        }

    }

    public static abstract class Tester<T> implements Callable<Void> {

        protected final ThreadLocalRandom random;
        protected final T object;
        protected final int[] ratios;
        protected final CountDownLatch latch;
        protected final long nbOps;
        protected ThreadLocal<Integer> name;
        protected final ThreadLocal<Integer> seq;
        public ConcurrentLinkedQueue<Integer> linkedQueue;

        public Tester(T object, int[] ratios, CountDownLatch latch, long nbOps) {
            this.random = ThreadLocalRandom.current();
            this.object = object;
            this.ratios = ratios;
            this.latch = latch;
            this.nbOps = nbOps;
            this.seq = new ThreadLocal<>();
            this.name = new ThreadLocal<>();
            linkedQueue = new ConcurrentLinkedQueue<>();
        }

        @Override
        public Void call() {

            seq.set(1);
            latch.countDown();
            name.set(Integer.parseInt(Thread.currentThread().getName().substring(5).replace("-thread-","")));
//            System.out.println(name.get());
            Long i = 0L;

            try{
                latch.await();
                while (flag.get()) {
                    test();
                }
                while (!Thread.currentThread().isInterrupted()) {
                    test();
                    i++;
                }
            } catch (InterruptedException | CancellationException e) {
                //ignore
            }

            nbOperations.add(i);

            return null;
        }

        protected abstract void test();
    }

    public static class CounterTester extends Tester<AbstractCounter> {

        public CounterTester(AbstractCounter counter, int[] ratios, CountDownLatch latch, long nbOps) {
            super(counter, ratios, latch, nbOps);
        }

        @Override
        protected void test() {
            if (random.nextInt(100) < ratios[0]) {
                object.increment();
            } else {
                object.read();
            }
        }
    }

    public static class DegradableCounterTester extends Tester<AbstractCounter> {

        public DegradableCounterTester(AbstractCounter counter, int[] ratios, CountDownLatch latch, long nbOps) {
            super(counter, ratios, latch, nbOps);
        }

        @Override
        protected void test() {
            if (random.nextInt(100) < ratios[0]) {
                object.increment();
            } else {
                object.read();
            }
        }
    }

    public static class CounterSnapshotTester extends Tester<AbstractCounter> {

        public CounterSnapshotTester(AbstractCounter object, int[] ratios, CountDownLatch latch, long nbOps) {
            super(object, ratios, latch, nbOps);
        }

        @Override
        protected void test() {
            if (random.nextInt(100) < ratios[0]) {
                object.increment();
            } else {
                object.read();
            }
        }
    }

    public static class CounterSnapshotV2Tester extends Tester<AbstractCounter> {

        public CounterSnapshotV2Tester(AbstractCounter object, int[] ratios, CountDownLatch latch, long nbOps) {
            super(object, ratios, latch, nbOps);
        }

        @Override
        protected void test() {
            if (random.nextInt(100) < ratios[0]) {
                object.increment();
            } else {
                object.read();
            }
        }
    }

    public static class ListTester extends Tester<eu.cloudbutton.dobj.types.AbstractList> {

        public ListTester(eu.cloudbutton.dobj.types.AbstractList list, int[] ratios, CountDownLatch latch, long nbOps) {
            super(list, ratios, latch, nbOps);
        }

        @Override
        protected void test() {
            int n = random.nextInt(100);

            if (n < ratios[0]) {
                object.append(n);
//                linkedQueue.add(n);
            } else {
                object.contains(n);
//                linkedQueue.contains(n);
            }
        }
    }

    public static class DegradableListTester extends Tester<eu.cloudbutton.dobj.types.AbstractList> {

        public DegradableListTester(eu.cloudbutton.dobj.types.AbstractList list, int[] ratios, CountDownLatch latch, long nbOps) {
            super(list, ratios, latch, nbOps);
        }

        @Override
        protected void test() {
            int n = random.nextInt(100);
            if (n < ratios[0]) {
                object.append(n);
            } else {
                object.contains(n);
            }
        }
    }

    public static class ListSnapshotTester extends Tester<eu.cloudbutton.dobj.types.AbstractList> {

        public ListSnapshotTester(eu.cloudbutton.dobj.types.AbstractList object, int[] ratios, CountDownLatch latch, long nbOps) {
            super(object, ratios, latch, nbOps);
        }

        @Override
        protected void test() {
            int n = random.nextInt(100);
            if (n < ratios[0]) {
                object.append(n);
            } else {
                object.contains(n);
            }
        }
    }

    public static class ListSnapshotV2Tester extends Tester<eu.cloudbutton.dobj.types.AbstractList> {

        public ListSnapshotV2Tester(eu.cloudbutton.dobj.types.AbstractList object, int[] ratios, CountDownLatch latch, long nbOps) {
            super(object, ratios, latch, nbOps);
        }

        @Override
        protected void test() {
            int n = random.nextInt(100);
            if (n < ratios[0]) {
                object.append(n);
            } else {
                object.contains(n);
            }
        }
    }

    public static class LinkedListTester extends Tester<eu.cloudbutton.dobj.types.AbstractList> {

        public LinkedListTester(eu.cloudbutton.dobj.types.AbstractList list, int[] ratios, CountDownLatch latch, long nbOps) {
            super(list, ratios, latch, nbOps);
        }

        @Override
        protected void test() {
            int n = random.nextInt(100);
            if (n < ratios[0]) {
                object.append(n);
            } else {
                object.contains(n);
            }
        }
    }

    public static class DegradableLinkedListTester extends Tester<eu.cloudbutton.dobj.types.AbstractList> {

        public DegradableLinkedListTester(eu.cloudbutton.dobj.types.AbstractList list, int[] ratios, CountDownLatch latch, long nbOps) {
            super(list, ratios, latch, nbOps);
        }

        @Override
        protected void test() {
            int n = random.nextInt(100);
            if (n < ratios[0]) {
                object.append(n);
            } else {
                object.contains(n);
            }
        }
    }

    public static class LinkedListSnapshotTester extends Tester<eu.cloudbutton.dobj.types.AbstractList> {

        public LinkedListSnapshotTester(eu.cloudbutton.dobj.types.AbstractList list, int[] ratios, CountDownLatch latch, long nbOps) {
            super(list, ratios, latch, nbOps);
        }

        @Override
        protected void test() {
            int n = random.nextInt(100);
            if (n < ratios[0]) {
                object.append(n);
            } else {
                object.contains(n);
            }
        }
    }

    public static class LinkedListSnapshotV2Tester extends Tester<eu.cloudbutton.dobj.types.AbstractList> {

        public LinkedListSnapshotV2Tester(eu.cloudbutton.dobj.types.AbstractList list, int[] ratios, CountDownLatch latch, long nbOps) {
            super(list, ratios, latch, nbOps);
        }

        @Override
        protected void test() {
            int n = random.nextInt(100);
            if (n < ratios[0]) {
                object.append(n);
            } else {
                object.contains(n);
            }
        }
    }

    public static class SetTester extends Tester<eu.cloudbutton.dobj.types.AbstractSet> {

        public SetTester(eu.cloudbutton.dobj.types.AbstractSet object, int[] ratios, CountDownLatch latch, long nbOps) {
            super(object, ratios, latch, nbOps);
        }

        @Override
        protected void test() {
            int n = random.nextInt(2* seq.get());
            if (n%100 < ratios[0]) {
                if (n%100 <= 50) {
                    seq.set(seq.get() + 1);
                    object.add("user_" + name.get() +"_"+ seq.get());
                }
                else
                    object.remove("user_"+name.get()+"_"+ n);
            } else {
                object.contains("user_"+name.get()+"_"+ n);
            }
        }
    }

    public static class DegradableSetTester extends Tester<eu.cloudbutton.dobj.types.AbstractSet> {

        public DegradableSetTester(eu.cloudbutton.dobj.types.AbstractSet object, int[] ratios, CountDownLatch latch, long nbOps) {
            super(object, ratios, latch, nbOps);
        }

        @Override
        protected void test() {
            int n = random.nextInt(2*seq.get());
            if (n%100 < ratios[0]) {
                if (n%100 <= 50) {
                    seq.set(seq.get() + 1);
                    object.add("user_" + name.get() + "_" + seq.get());
                }
                else
                    object.remove("user_"+name.get()+"_" + n);
            } else {
                object.contains("user_"+name.get()+"_" + n);
            }
        }
    }

    public static class SetSnapshotTester extends Tester<eu.cloudbutton.dobj.types.AbstractSet> {

        public SetSnapshotTester(eu.cloudbutton.dobj.types.AbstractSet object, int[] ratios, CountDownLatch latch, long nbOps) {
            super(object, ratios, latch, nbOps);
        }

        @Override
        protected void test() {
            int n = random.nextInt(2*seq.get());
            if (n%100 < ratios[0]) {
                if (n%100 <= 50) {
                    seq.set(seq.get() + 1);
                    object.add("user_" + name.get() + "_" + seq.get());
                }
                else
                    object.remove("user_"+name.get()+"_" + n);
            } else {
                object.contains("user_"+name.get()+"_" + n);
            }
        }
    }

    public static class SetSnapshotV2Tester extends Tester<eu.cloudbutton.dobj.types.AbstractSet> {

        public SetSnapshotV2Tester(eu.cloudbutton.dobj.types.AbstractSet object, int[] ratios, CountDownLatch latch, long nbOps) {
            super(object, ratios, latch, nbOps);
        }

        @Override
        protected void test() {
            int n = random.nextInt(2*seq.get());
            if (n%100 < ratios[0]) {
                if (n%100 <= 50) {
                    seq.set(seq.get() + 1);
                    object.add("user_" + name.get() + "_" + seq.get());
                }
                else
                    object.remove("user_"+name.get()+"_" + n);
            } else {
                object.contains("user_"+name.get()+"_" + n);
            }
        }
    }

    public static class SecondDegradableListTester extends Tester<eu.cloudbutton.dobj.types.AbstractList> {

        public SecondDegradableListTester(eu.cloudbutton.dobj.types.AbstractList list, int[] ratios, CountDownLatch latch, long nbOps) {
            super(list, ratios, latch, nbOps);
        }

        @Override
        protected void test() {
            int n = random.nextInt(100);
            if (n < ratios[0]) {
                if (n <= 50)
                    object.append(n);
                else
                    object.remove(n);
            } else {
                object.contains(n);
            }
        }
    }

    public static class ThirdDegradableListTester extends Tester<eu.cloudbutton.dobj.types.AbstractList> {

        public ThirdDegradableListTester(eu.cloudbutton.dobj.types.AbstractList list, int[] ratios, CountDownLatch latch, long nbOps) {
            super(list, ratios, latch, nbOps);
        }

        @Override
        protected void test() {
            int n = random.nextInt(100);
            if (n < ratios[0]) {
                if (n <= 50)
                    object.append(n);
                else
                    object.remove(n);
            } else {
                object.contains(n);
            }
        }
    }

    public class Coordinator implements Callable<Void> {
        @Override
        public Void call() throws Exception {
            try {
                TimeUnit.SECONDS.sleep(wTime);
                flag.set(false);
//                System.out.println("GO !!");
            } catch (InterruptedException e) {
                throw new Exception("Thread interrupted", e);
            }
            return null;
        }
    }
}
