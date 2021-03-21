package eu.cloudbutton.dobj;

import eu.cloudbutton.dobj.types.AbstractCounter;
import eu.cloudbutton.dobj.types.Factory;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.StringArrayOptionHandler;
import org.testng.annotations.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;

import static java.lang.System.exit;
import static org.kohsuke.args4j.OptionHandlerFilter.ALL;

public class Benchmark {

    @Option(name = "-type", required = true, usage = "type to test")
    private String type;

    @Option(name = "-ratios", required = true, handler = StringArrayOptionHandler.class, usage = "ratios")
    private String[] ratios;

    @Option(name = "-ow", usage = "overwrite")
    private Boolean overwrite;

    public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {
        new Benchmark().counter(args);
    }

    @Test
    public void counter(String[] args) throws InterruptedException, ExecutionException, IOException {
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

        File file = new File("results_"+type+".txt");

        if(!file.exists()){
            file.createNewFile();
        }else {
            if(overwrite == null) {
                System.out.println("The file " + file.getName() + " already exists. Please re-run with option -ow to overwrite.");
                exit(0);
            }
        }

        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);

        final int nbOps = 100000000;
//        final int nbThreads = Runtime.getRuntime().availableProcessors()/2;
        final int nbThreads = 40;
        final int nbTest = 10;

        try {

            Factory factory = new Factory();
            String constructor = "create"+type;
            Object object = Factory.class.getDeclaredMethod(constructor).invoke(factory);

            List<Double> result = new ArrayList<>();
            Map<Integer, List<Double>> results = new HashMap<>();

            for (int i = 1; i <= nbThreads; i++) {
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
                    double startTime = System.nanoTime();
                    List<Future<Void>> futures = executor.invokeAll(callables);
                    for (Future<Void> future : futures) {
                        future.get();
                    }
                    double endTime = System.nanoTime();
                    double duration = endTime - startTime;

                    // report
                    // System.out.println(count.read()+" operations; "+i+" threads");
                    System.out.println(duration+" time per op: "+ duration/(((double)nbOps)/((double)i))+"ns");
                    result.add(duration);

                    executor.shutdown();

                    object = Factory.class.getDeclaredMethod(constructor).invoke(factory);

                }
                results.put(i, result);
                result = new ArrayList<>();
            }

            List<Double> avg_result = new ArrayList<>();
            System.out.println(results);

            for (List<Double> l : results.values()) {
                Double sum = 0.0;
                for (Double d: l) {
                    sum += d;
                }
                avg_result.add(sum/l.size());
                bw.write(String.valueOf((sum/l.size())));
                bw.newLine();
            }

            System.out.println(avg_result);

            bw.close();
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }


    }

    public static class FactoryTester {

        private final Object object;
        private final int[] ratios;
        private final CountDownLatch latch;
        private final int nbOps;

        FactoryTester(Object object, int[] ratios, CountDownLatch latch, int nbOps){
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

        public ListTester createListTester(){
            return new ListTester((eu.cloudbutton.dobj.types.AbstractList) object, ratios, latch, nbOps);
        }

        public DegradableListTester createDegradableListTester(){
            return new DegradableListTester((eu.cloudbutton.dobj.types.AbstractList) object, ratios, latch, nbOps);
        }

        public SetTester createSetTester(){
            return new SetTester((eu.cloudbutton.dobj.types.AbstractSet) object, ratios, latch, nbOps);
        }

        public DegradableSetTester createDegradableSetTester(){
            return new DegradableSetTester((eu.cloudbutton.dobj.types.AbstractSet) object, ratios, latch, nbOps);
        }

    }


    public static abstract class Tester<T> implements Callable<Void> {

        protected final ThreadLocalRandom random;
        protected final T object;
        protected final int[] ratios;
        protected final CountDownLatch latch;
        protected final int nbOps;

        public Tester(T object, int[] ratios, CountDownLatch latch, int nbOps) {
            this.random =  ThreadLocalRandom.current();
            this.object = object;
            this.ratios = ratios;
            this.latch = latch;
            this.nbOps = nbOps;
        }

        @Override
        public Void call() {
            latch.countDown();
            try {
                latch.await();
                for (int i = 0; i < nbOps; i++) {
                    test();
                }
            } catch (InterruptedException e) {
                //ignore
            }
            return null;
        }

        protected abstract void test();
    }

    public static class CounterTester extends Tester<AbstractCounter> {

        public CounterTester(AbstractCounter counter, int[] ratios, CountDownLatch latch, int nbOps) {
            super(counter, ratios, latch, nbOps);
        }

        @Override
        protected void test() {
            if (random.nextFloat()<=ratios[0]){
                object.increment();
            } else {
                object.read();
            }
        }
    }

    public static class DegradableCounterTester extends Tester<AbstractCounter> {

        public DegradableCounterTester(AbstractCounter counter, int[] ratios, CountDownLatch latch, int nbOps) {
            super(counter, ratios, latch, nbOps);
        }

        @Override
        protected void test() {
            if (random.nextFloat()<=ratios[0]){
                object.increment();
            } else {
                object.read();
            }
        }
    }

    public static class ListTester extends Tester<eu.cloudbutton.dobj.types.AbstractList> {

        public ListTester(eu.cloudbutton.dobj.types.AbstractList list, int[] ratios, CountDownLatch latch, int nbOps) {
            super(list, ratios, latch, nbOps);
        }

        @Override
        protected void test() {
            if (random.nextFloat()<=ratios[0]){
                object.append("0");
            } else {
                object.read();
            }
        }
    }

    public static class DegradableListTester extends Tester<eu.cloudbutton.dobj.types.AbstractList> {

        public DegradableListTester(eu.cloudbutton.dobj.types.AbstractList list, int[] ratios, CountDownLatch latch, int nbOps) {
            super(list, ratios, latch, nbOps);
        }

        @Override
        protected void test() {
            if (random.nextFloat()<=ratios[0]){
                object.append("0");
            } else {
                object.read();
            }
        }
    }

    public static class SetTester extends Tester<eu.cloudbutton.dobj.types.AbstractSet>{

        public SetTester(eu.cloudbutton.dobj.types.AbstractSet object, int[] ratios, CountDownLatch latch, int nbOps) {
            super(object, ratios, latch, nbOps);
        }

        @Override
        protected void test(){
            float n = random.nextFloat();
            if (n<=ratios[0]){
                object.add(String.valueOf(n));
            } else {
                object.read();
            }
        }
    }

    public static class DegradableSetTester extends Tester<eu.cloudbutton.dobj.types.AbstractSet>{

        public DegradableSetTester(eu.cloudbutton.dobj.types.AbstractSet object, int[] ratios, CountDownLatch latch, int nbOps) {
            super(object, ratios, latch, nbOps);
        }

        @Override
        protected void test(){
            float n = random.nextFloat();
            if (n<=ratios[0]){
                object.add(String.valueOf(n));
            } else {
                object.read();
            }
        }
    }


}
