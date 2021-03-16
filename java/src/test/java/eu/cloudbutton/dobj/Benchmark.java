package eu.cloudbutton.dobj;

import eu.cloudbutton.dobj.types.Counter;
import eu.cloudbutton.dobj.types.CounterFactory;
import org.testng.annotations.Test;

/*import static org.kohsuke.args4j.ExampleMode.ALL;
import org.kohsuke.args4j.*;
import org.kohsuke.args4j.spi.BooleanOptionHandler;*/

//import com.github.sh0nk.matplotlib4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class Benchmark {

/*
    @Option(name = "-type",usage = "give the type to test")
    private String type;

    @Argument
    private List<String> arguments = new ArrayList<String>();
*/

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        new Benchmark().counter();
    }

    @Test
    public void counter() throws InterruptedException, ExecutionException {
       /* CmdLineParser parser = new CmdLineParser(this);

        try {
            // parse the arguments.
            parser.parseArgument(args);

            // you can parse additional arguments if you want.
            // parser.parseArgument("more","args");

            // after parsing arguments, you should check
            // if enough arguments are given.
            if( arguments.isEmpty() )
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
        }*/
        final int nbOps = 1000000;
//        final int nbThreads = Runtime.getRuntime().availableProcessors()/2;
        final int nbThreads = 5;
        final int nbTest = 5;

        CounterFactory counterFactory = new CounterFactory();
        // Counter count = counterFactory.createjavacounter();
        Counter count = counterFactory.createdegradablecounter();

        List<Double> result = new ArrayList<>();
        Map<Integer, List<Double>> results = new HashMap<>();

        for (int i = 1; i <= nbThreads; i++) {
            for (int a = 0; a < nbTest; a++) {
                List<Callable<Void>> callables = new ArrayList<>();
                ExecutorService executor = Executors.newFixedThreadPool(i);

                CountDownLatch latch = new CountDownLatch(i);
                for (int j = 0; j < i; j++) {
                    callables.add(new testCallable(count, latch, nbOps / i));
                }

                double startTime = System.nanoTime();

                List<Future<Void>> futures = executor.invokeAll(callables);
                for (Future<Void> future : futures) {
                    future.get();
                }
                double endTime = System.nanoTime();
                double duration = endTime - startTime;
                System.out.println(count.read()+" operations; "+i+" threads");
                System.out.println(duration+" time per op: "+ duration/(((double)nbOps)/((double)i))+"ns");
                result.add(duration);
                executor.shutdown();
                // count = counterFactory.createjavacounter();
                count = counterFactory.createdegradablecounter();
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
    }


    private static class testCallable implements Callable<Void> {

        private final Counter count;
        private final CountDownLatch latch;
        private final int nbOps;

        public testCallable(Counter count, CountDownLatch latch, int nbOps) {
            this.count = count;
            this.latch = latch;
            this.nbOps = nbOps;
        }


        @Override
        public Void call() {

            latch.countDown();
            try {
                latch.await();
                this.count.increment();
                for (int i = 0; i < nbOps; i++) {
                    this.count.read();
                }
            } catch (InterruptedException e) {
                //ignore
            }
            return null;
        }
    }


}
