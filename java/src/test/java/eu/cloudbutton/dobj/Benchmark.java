package eu.cloudbutton.dobj;

import eu.cloudbutton.dobj.types.*;
import eu.cloudbutton.dobj.types.AbstractList;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.StringArrayOptionHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.kohsuke.args4j.OptionHandlerFilter.ALL;

public class Benchmark {

    private static ConcurrentLinkedQueue<Long> nbOperations = new ConcurrentLinkedQueue<>();
    private static ConcurrentLinkedQueue<Integer> sizes = new ConcurrentLinkedQueue<>();
    private static int nbAppend = 0;
    private static int nbRemove = 0;

    private static AtomicBoolean flag;
    @Option(name = "-type", required = true, usage = "type to test")
    private String type;
    @Option(name = "-ratios", handler = StringArrayOptionHandler.class, usage = "ratios")
    private String[] ratios = {"100"};
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

            Class clazz = Class.forName(type);
            String constructor = "create" + clazz.getSimpleName();

            for (int i = 1; i <= nbThreads; ) {
                System.out.println("Nb threads = " + i);
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
                        Method m = factoryTester.getClass().getDeclaredMethod("create" + clazz.getSuperclass().getSimpleName() + "Tester");
                        Tester tester = (Tester) m.invoke(factoryTester);
                        callables.add(tester);
                    }

                    FactoryTester factoryT = new FactoryTester(
                            object,
                            Arrays.stream(ratios).mapToInt(Integer::parseInt).toArray(),
                            latch,
                            nbOps/i);

                    Method m1 = factoryT.getClass().getDeclaredMethod("create" + clazz.getSuperclass().getSimpleName() + "Tester");
                    Tester t = (Tester) m1.invoke(factoryT);
                    callables.add(t);

                    ExecutorService executorService = Executors.newFixedThreadPool(1);
                    flag = new AtomicBoolean();
                    flag.set(true);
                    executorService.submit(new Coordinator());

                    List<Future<Void>> futures;

//                    System.out.println("Max JVM memory: " + Runtime.getRuntime().maxMemory());

                    // launch computation
                    futures = executor.invokeAll(callables);
                    try{
                        for (Future<Void> future : futures) {
                            future.get();
                        }
                    } catch (CancellationException e) {
                        //ignore
                        System.out.println(e);
                    }

                    executor.shutdownNow();
                    TimeUnit.SECONDS.sleep(1);
/*
                    if (type.equals("List") ){
                        eu.cloudbutton.dobj.types.List list = (eu.cloudbutton.dobj.types.List) object;
                        sizes.add(list.read().size());
                        System.out.println("Size of List : " + list.read().size());
                    }else if (type.equals("DegradableList") ){
                        eu.cloudbutton.dobj.types.DegradableList list = (eu.cloudbutton.dobj.types.DegradableList) object;
                        sizes.add(list.read().size());
                        System.out.println("Size of DegradableList : " + list.read().size());
                    }else if (type.equals("DegradableLinkedList") ){
                        eu.cloudbutton.dobj.types.DegradableLinkedList list = (eu.cloudbutton.dobj.types.DegradableLinkedList) object;
                        sizes.add(list.read().size());
                        System.out.println("Size of DegradableLinkedList : " + list.read().size());
                    }else if (type.equals("LinkedList") ){
                        eu.cloudbutton.dobj.types.LinkedList list = (eu.cloudbutton.dobj.types.LinkedList) object;
                        sizes.add(list.read().size());
                        System.out.println("Size of LinkedList : " + list.read().size());
                    }else if (type.equals("Counter") ){
                        Counter counter = (Counter) object;
                        sizes.add(counter.read());
                        System.out.println("Size of Counter : " + counter.read());
                    }else if (type.equals("DegradableCounter") ){
                        DegradableCounter counter = (DegradableCounter) object;
                        sizes.add(counter.read());
                        System.out.println("Size of DegradableCounter : " + counter.read());
                    }else if (type.equals("Set") ){
                        eu.cloudbutton.dobj.types.Set set = (eu.cloudbutton.dobj.types.Set) object;
                        System.out.println("Size of Set : " + set.read().size());
                        sizes.add(set.read().size());
                    }else if (type.equals("DegradableSet") ){
                        eu.cloudbutton.dobj.types.DegradableSet set = (eu.cloudbutton.dobj.types.DegradableSet) object;
                        System.out.println("Size of DegradableSet : " + set.read().size());
                        sizes.add(set.read().size());
                    }*/
                }
                Long sum = 0L;
                int sum2 = 0;
                for (Long val : nbOperations) {
                    sum += val;
                }

                for (int val : sizes)
                    sum2 += val;

                double avg_op = sum / i;
                System.out.println(i + " " + (time) / avg_op); // printing the avg time per op for i thread(s)
//                System.out.println("Avg size : " + sum2/ sizes.size());
                nbOperations = new ConcurrentLinkedQueue<>();
//                sizes = new ConcurrentLinkedQueue<>();

                i = 2 * i;

                /*if(i==2)
                    i = nbThreads;
*/
                if (i > nbThreads && i != 2 * nbThreads) {
                    i = nbThreads;
                }
            }

        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | ClassNotFoundException e) {
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

        public NoopTester createNoopTester() {
            return new NoopTester((eu.cloudbutton.dobj.types.Noop) object, ratios, latch, nbOps);
        }

        public CounterTester createAbstractCounterTester() {
            return new CounterTester((AbstractCounter) object, ratios, latch, nbOps);
        }

        public SetTester createAbstractSetTester() {
            return new SetTester((AbstractSet) object, ratios, latch, nbOps);
        }

        public ListTester createAbstractListTester() {
            return new ListTester((AbstractList) object, ratios, latch, nbOps);
        }

        public QueueTester createAbstractQueueTester() {
            return new QueueTester((AbstractQueue) object, ratios, latch, nbOps);
        }

    }

    public static abstract class Tester<T> implements Callable<Void> {

        protected static final int ITEM_PER_THREAD = 1;

        protected final ThreadLocalRandom random;
        protected final T object;
        protected final int[] ratios;
        protected final CountDownLatch latch;
        protected final long nbOps;

        public Tester(T object, int[] ratios, CountDownLatch latch, long nbOps) {
            this.random = ThreadLocalRandom.current();
            this.object = object;
            this.ratios = ratios;
            this.latch = latch;
            this.nbOps = nbOps;
        }

        @Override
        public Void call() {

            latch.countDown();
            long i = 0L;

            try{
                latch.await();

                // warm up
                while (flag.get()) {
                    test();
                }

                // compute
                while (!flag.get()) {
                    test();
                    i++;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            nbOperations.add(i);

            return null;
        }

        protected abstract void test();
    }

    public static class NoopTester extends Tester<Noop> {

        public NoopTester(Noop nope, int[] ratios, CountDownLatch latch, long nbOps){
            super(nope, ratios, latch, nbOps);
        }

        @Override
        protected void test(){
            // no-op
            int n = random.nextInt(ITEM_PER_THREAD);
            if (n%101 <= ratios[0]) {
                if (n%101 <= 50) {
                    n++;
                } else {
                    n--;
                }
            } else {
                n+=2;
            }
        }
    }

    public static class CounterTester extends Tester<AbstractCounter> {

        public CounterTester(AbstractCounter counter, int[] ratios, CountDownLatch latch, long nbOps) {
            super(counter, ratios, latch, nbOps);
        }

        @Override
        protected void test() {
            if (random.nextInt(101) <= ratios[0]) {
                object.increment();
            } else {
                object.read();
            }
        }
    }

    public static class SetTester extends Tester<AbstractSet> {

        public SetTester(AbstractSet object, int[] ratios, CountDownLatch latch, long nbOps) {
            super(object, ratios, latch, nbOps);
        }

        @Override
        protected void test() {
            int n = random.nextInt(ITEM_PER_THREAD);
            long iid = Thread.currentThread().getId()*1000000000L+n;
            if (n%101 <= ratios[0]) {
                if (n%101 <= 50) {
                    object.add(iid);
                }else{
                    object.remove(iid);
                }
            } else {
                object.contains(iid);
            }
        }
    }

    public static class ListTester extends Tester<AbstractList> {

        public ListTester(AbstractList list, int[] ratios, CountDownLatch latch, long nbOps) {
            super(list, ratios, latch, nbOps);
        }

        @Override
        protected void test() {
            int n = random.nextInt(ITEM_PER_THREAD);
            int i = random.nextInt(2);
            long iid = Thread.currentThread().getId()*1000000000L+n;
            if (n%101 <= ratios[0]) {
                if (i == 0) {
                    object.append(iid);
                }else {
                    object.remove(iid);
                }
            } else {
                object.contains(iid);
            }
        }
    }

    public static class QueueTester extends Tester<AbstractQueue> {

        public QueueTester(AbstractQueue queue, int[] ratios, CountDownLatch latch, long nbOps) {
            super(queue, ratios, latch, nbOps);
        }

        @Override
        protected void test() {
            int n = random.nextInt(ITEM_PER_THREAD);
            int i = random.nextInt(2);
            long iid = Thread.currentThread().getId()*1000000000L+n;
            if (n%101 <= ratios[0]) {
                if (i == 0) {
                    object.offer(iid);
                }else {
                    object.poll();
                }
            } else {
                object.contains(iid);
            }
        }
    }


    public class Coordinator implements Callable<Void> {

        public Coordinator(){}

        @Override
        public Void call() throws Exception {
            try {
                TimeUnit.SECONDS.sleep(wTime);
                flag.set(false);
                TimeUnit.SECONDS.sleep(time);
                flag.set(true);


            } catch (InterruptedException e) {
                throw new Exception("Thread interrupted", e);
            }
            return null;
        }
    }
}