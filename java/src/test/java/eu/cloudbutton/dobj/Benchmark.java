package eu.cloudbutton.dobj;

import eu.cloudbutton.dobj.types.Counter;
import eu.cloudbutton.dobj.types.Factory;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.StringArrayOptionHandler;
import org.testng.annotations.Test;

/*import static org.kohsuke.args4j.ExampleMode.ALL;
import org.kohsuke.args4j.*;
import org.kohsuke.args4j.spi.BooleanOptionHandler;*/

//import com.github.sh0nk.matplotlib4j;

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

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        new Benchmark().counter(args);
    }

    @Test
    public void counter(String[] args) throws InterruptedException, ExecutionException {
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

        final int nbOps = 1000000;
//        final int nbThreads = Runtime.getRuntime().availableProcessors()/2;
        final int nbThreads = 5;
        final int nbTest = 5;

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
            }
            System.out.println(avg_result);

/*        Plot plt = Plot.create();
        plt.plot()
                .add(Arrays.asList(1.3, 2))
                .label("label")
                .linestyle("--");
        plt.xlabel("xlabel");
        plt.ylabel("ylabel");
        plt.text(0.5, 0.2, "text");
        plt.title("Title!");
        plt.legend();
        plt.show();*/

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }


    }

    public static class FactoryTester {

        private Object object;
        private int[] ratios;
        private CountDownLatch latch;
        private int nbOps;

        FactoryTester(Object object, int[] ratios, CountDownLatch latch, int nbOps){
            this.object = object;
            this.ratios = ratios;
            this.latch = latch;
            this.nbOps = nbOps;
        }

        public CounterTester createCounterTester(){
            return new CounterTester((Counter) object, ratios, latch, nbOps);
        }

        public ListTester createListTester(){
            return new ListTester((eu.cloudbutton.dobj.types.List) object, ratios, latch, nbOps);
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

    public static class CounterTester extends Tester<Counter> {

        public CounterTester(Counter counter, int[] ratios, CountDownLatch latch, int nbOps) {
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

    public static class ListTester extends Tester<eu.cloudbutton.dobj.types.List> {

        public ListTester(eu.cloudbutton.dobj.types.List counter, int[] ratios, CountDownLatch latch, int nbOps) {
            super(counter, ratios, latch, nbOps);
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


}
