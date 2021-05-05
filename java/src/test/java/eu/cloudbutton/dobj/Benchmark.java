package eu.cloudbutton.dobj;

import eu.cloudbutton.dobj.types.AbstractCounter;
import eu.cloudbutton.dobj.types.Factory;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.StringArrayOptionHandler;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;

import static org.kohsuke.args4j.OptionHandlerFilter.ALL;

public class Benchmark {

    @Option(name = "-type", required = true, usage = "type to test")
    private String type;

    @Option(name = "-ratios", required = true, handler = StringArrayOptionHandler.class, usage = "ratios")
    private String[] ratios;

    @Option(name = "-nbThreads", usage = "Number of threads")
    private int nbThreads = Runtime.getRuntime().availableProcessors()/2;

    @Option(name = "-nbOps", usage = "Number of operations between two time's read")
    private long nbOps = 100_000_000;

    @Option(name = "-nbTest", usage = "Number of test")
    private int nbTest = 1;

    private static List<Double> durations = new ArrayList();

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        new Benchmark().doMain(args);
    }

    @Test
    public void doMain(String[] args) throws InterruptedException, ExecutionException {
       CmdLineParser parser = new CmdLineParser(this);

        try {
            // parse the arguments.
            parser.parseArgument(args);

            // you can parse additional arguments if you want.
            // parser.parseArgument("more","args");

            // after parsing arguments, you should check
            // if enough arguments are given.
            if( args.length < 1 )
                throw new CmdLineException(parser,"No argument is given");

        } catch( CmdLineException e ) {
            // if there's a problem in the command line,
            // you'll get this exception. this will report
            // an error message.
            System.err.println(e.getMessage());
            System.err.println("java SampleMain [options...] arguments...");
            // print the list of available options
            parser.printUsage(System.err);
            System.err.println();

            // print option sample. This is useful some time
            System.err.println("  Example: java Benchmark"+ parser.printExample(ALL));

            return;
        }

        List<Future<Void>> futures = null;

        try {

            Factory factory = new Factory();
            String constructor = "create"+type;
            Object object = Factory.class.getDeclaredMethod(constructor).invoke(factory);

            List<Double> result = new ArrayList<>();

            for (int i = 1; i <= nbThreads; ) {
                for (int a = 0; a < nbTest; a++) {
                    List<Callable<Void>> callables = new ArrayList<>();
                    ExecutorService executor = Executors.newFixedThreadPool(i);

                    CountDownLatch latch = new CountDownLatch(i);
                    FactoryTester factoryTester = new FactoryTester(
                            object,
                            Arrays.stream(ratios).mapToInt(Integer::parseInt).toArray(),
                            latch,nbOps / i);
                    for (int j = 0; j < i; j++) {
                        Method m = factoryTester.getClass().getDeclaredMethod("create"+type+"Tester");
                        Tester tester = (Tester) m.invoke(factoryTester);
                        callables.add(tester);
                    }

                    // launch computation
                    futures = executor.invokeAll(callables, 2, TimeUnit.SECONDS);
                    for (Future<Void> future : futures) {
                        future.get();
                    }

                    System.out.println(durations);
                    // report
                    // System.out.println(duration+" time per op: "+ duration/(((double)nbOps)/((double)i))+"ns");
                    result.add(durations.size()/((double)nbOps*1/3));

                    executor.shutdown();

                    object = Factory.class.getDeclaredMethod(constructor).invoke(factory);

                }
                Double sum = 0.0;
                for (Double d: result){
                    sum += d;
                }
                System.out.println(i + " "+ sum/result.size()); // printing the avg time per op for i thread(s)
                result = new ArrayList<>();

                i=2*i;
                if (i > nbThreads && i != 2*nbThreads)
                    i = nbThreads;
            }

        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        } catch (CancellationException e) {
            System.out.println("error");
            for (Future<Void> future : futures) {
                future.cancel(true);
                System.out.println(future.isCancelled());
                System.out.println(future.isDone());
            }
//            e.printStackTrace();
        }
    }

    public static class FactoryTester {

        private final Object object;
        private final int[] ratios;
        private final CountDownLatch latch;
        private final long nbOps;

        FactoryTester(Object object, int[] ratios, CountDownLatch latch, long nbOps){
            this.object = object;
            this.ratios = ratios;
            this.latch = latch;
            this.nbOps = nbOps;
        }

        public CounterTester createCounterTester(){
            return new CounterTester((AbstractCounter) object, ratios, latch, nbOps);
        }

        public DegradableCounterTester createDegradableCounterTester(){
            return new DegradableCounterTester((AbstractCounter) object, ratios, latch, nbOps);
        }

        public CounterSnapshotTester createCounterSnapshotTester(){
            return new CounterSnapshotTester((AbstractCounter) object, ratios, latch, nbOps);
        }

        public ListTester createListTester(){
            return new ListTester((eu.cloudbutton.dobj.types.AbstractList) object, ratios, latch, nbOps);
        }

        public DegradableListTester createDegradableListTester(){
            return new DegradableListTester((eu.cloudbutton.dobj.types.AbstractList) object, ratios, latch, nbOps);
        }

        public ListSnapshotTester createListSnapshotTester(){
            return new ListSnapshotTester((eu.cloudbutton.dobj.types.AbstractList) object, ratios, latch, nbOps);
        }

        public SetTester createSetTester(){
            return new SetTester((eu.cloudbutton.dobj.types.AbstractSet) object, ratios, latch, nbOps);
        }

        public DegradableSetTester createDegradableSetTester(){
            return new DegradableSetTester((eu.cloudbutton.dobj.types.AbstractSet) object, ratios, latch, nbOps);
        }

        public SetSnapshotTester createSetSnapshotTester(){
            return new SetSnapshotTester((eu.cloudbutton.dobj.types.AbstractSet) object, ratios, latch, nbOps);
        }

        public SecondDegradableListTester createSecondDegradableListTester(){
            return new SecondDegradableListTester((eu.cloudbutton.dobj.types.AbstractList) object, ratios, latch, nbOps);
        }

        public ThirdDegradableListTester createThirdDegradableListTester(){
            return new ThirdDegradableListTester((eu.cloudbutton.dobj.types.AbstractList) object, ratios, latch, nbOps);
        }

    }


    public static abstract class Tester<T> implements Callable<Void> {

        protected final ThreadLocalRandom random;
        protected final T object;
        protected final int[] ratios;
        protected final CountDownLatch latch;
        protected final long nbOps;

        public Tester(T object, int[] ratios, CountDownLatch latch, long nbOps) {
            this.random =  ThreadLocalRandom.current();
            this.object = object;
            this.ratios = ratios;
            this.latch = latch;
            this.nbOps = nbOps;
        }

        @Override
        public Void call() {
            latch.countDown();
            double startTime = 0;
            double endTime = 0;
            double realStart = System.nanoTime();
            try {
                latch.await();
//                System.out.println("nb d'ope effectué par proc : " + (double)1/3*nbOps);
//                System.out.println("2/3 de nbOps = " + (double)2/3*nbOps);
//                for (int i = 0; i < nbOps; i++) {
                int i = 0;
                while(true){
                    if (i == (double)1/3*nbOps) {
                        startTime = System.nanoTime();
//                        System.out.println("1/3 = "+i);
                    }
                    if (i == (double)2/3*nbOps) {
                        endTime = System.nanoTime();
//                        System.out.println("2/3 = " + i);
                    }
                    test();
                    i+=1;
                    if(i%10000000==0)
                        System.out.println(i);
                }
            } catch (InterruptedException e) {
                //ignore
            }
            double duration = endTime - startTime;
            durations.add(duration);

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
            if (random.nextFloat()<ratios[0]){
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
            if (random.nextFloat()<ratios[0]){
                object.increment();
            } else {
                object.read();
            }
        }
    }

    public static class CounterSnapshotTester extends Tester<AbstractCounter>{

        public CounterSnapshotTester(AbstractCounter object, int[] ratios, CountDownLatch latch, long nbOps) {
            super(object, ratios, latch, nbOps);
        }

        @Override
        protected void test() {
            if (random.nextFloat()<ratios[0]){
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
            if (random.nextFloat()<ratios[0]){
                object.append("0");
            } else {
                object.read();
            }
        }
    }

    public static class DegradableListTester extends Tester<eu.cloudbutton.dobj.types.AbstractList> {

        public DegradableListTester(eu.cloudbutton.dobj.types.AbstractList list, int[] ratios, CountDownLatch latch, long nbOps) {
            super(list, ratios, latch, nbOps);
        }

        @Override
        protected void test() {
            if (random.nextFloat()<ratios[0]){
                object.append("0");
            } else {
                object.read();
            }
        }
    }

    public static class ListSnapshotTester extends Tester<eu.cloudbutton.dobj.types.AbstractList>{

        public ListSnapshotTester(eu.cloudbutton.dobj.types.AbstractList object, int[] ratios, CountDownLatch latch, long nbOps) {
            super(object, ratios, latch, nbOps);
        }

        @Override
        protected void test() {
            if (random.nextFloat()<ratios[0]){
                object.append("0");
            } else {
                object.read();
            }
        }
    }

    public static class SetTester extends Tester<eu.cloudbutton.dobj.types.AbstractSet>{

        public SetTester(eu.cloudbutton.dobj.types.AbstractSet object, int[] ratios, CountDownLatch latch, long nbOps) {
            super(object, ratios, latch, nbOps);
        }

        @Override
        protected void test(){
            float n = random.nextFloat();
            if (n<ratios[0]){
                object.add(n);
            } else {
                object.read();
            }
        }
    }

    public static class DegradableSetTester extends Tester<eu.cloudbutton.dobj.types.AbstractSet>{

        public DegradableSetTester(eu.cloudbutton.dobj.types.AbstractSet object, int[] ratios, CountDownLatch latch, long nbOps) {
            super(object, ratios, latch, nbOps);
        }

        @Override
        protected void test(){
            float n = random.nextFloat();
            if (n<ratios[0]){
                object.add(n);
            } else {
                object.read();
            }
        }
    }

    public static class SetSnapshotTester extends Tester<eu.cloudbutton.dobj.types.AbstractSet>{

        public SetSnapshotTester(eu.cloudbutton.dobj.types.AbstractSet object, int[] ratios, CountDownLatch latch, long nbOps) {
            super(object, ratios, latch, nbOps);
        }

        @Override
        protected void test(){
            float n = random.nextFloat();
            if (n<ratios[0]){
                object.add(n);
            } else {
                object.read();
            }
        }
    }

    public static class SecondDegradableListTester extends Tester<eu.cloudbutton.dobj.types.AbstractList> {

        public SecondDegradableListTester(eu.cloudbutton.dobj.types.AbstractList list, int[] ratios, CountDownLatch latch, long nbOps) {
            super(list, ratios, latch, nbOps);
        }

        @Override
        protected void test() {
            if (random.nextFloat()<ratios[0]){
                object.append("0");
            } else {
                object.read();
            }
        }
    }

    public static class ThirdDegradableListTester extends Tester<eu.cloudbutton.dobj.types.AbstractList> {

        public ThirdDegradableListTester(eu.cloudbutton.dobj.types.AbstractList list, int[] ratios, CountDownLatch latch, long nbOps) {
            super(list, ratios, latch, nbOps);
        }

        @Override
        protected void test() {
            if (random.nextFloat()<ratios[0]){
                object.append("0");
            } else {
                object.read();
            }
        }
    }
}
