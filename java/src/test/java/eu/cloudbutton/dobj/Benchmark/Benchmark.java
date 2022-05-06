package eu.cloudbutton.dobj.Benchmark;

import eu.cloudbutton.dobj.Benchmark.Tester.FactoryFiller;
import eu.cloudbutton.dobj.Benchmark.Tester.FactoryTester;
import eu.cloudbutton.dobj.Benchmark.Tester.Filler;
import eu.cloudbutton.dobj.Benchmark.Tester.Tester;
import eu.cloudbutton.dobj.counter.FuzzyCounter;
import eu.cloudbutton.dobj.Factory;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.ExplicitBooleanOptionHandler;
import org.kohsuke.args4j.spi.StringArrayOptionHandler;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static org.kohsuke.args4j.OptionHandlerFilter.ALL;

public class Benchmark {

    public static AtomicLong timeAdd;
    public static AtomicLong timeRemove;
    public static AtomicLong timeRead;
    public static AtomicLong nbAdd;
    public static AtomicLong nbRemove;
    public static AtomicLong nbRead;
    public static AtomicBoolean flag;

    @Option(name = "-type", required = true, usage = "type to test")
    private String type;
    @Option(name = "-ratios", handler = StringArrayOptionHandler.class, usage = "ratios")
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
    @Option(name = "-s", handler = ExplicitBooleanOptionHandler.class, usage = "Save the result")
    private boolean _s = false;
    @Option(name = "-p", handler = ExplicitBooleanOptionHandler.class, usage = "Print the result")
    private boolean _p = false;

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

            PrintWriter printWriter = null;
            FileWriter fileWriter;

            for (int nbCurrentThread = 1; nbCurrentThread <= nbThreads; ) {
                nbAdd = new AtomicLong(0);
                nbRemove = new AtomicLong(0);
                nbRead = new AtomicLong(0);

                timeAdd = new AtomicLong(0);
                timeRemove = new AtomicLong(0);
                timeRead = new AtomicLong(0);
                for (int a = 0; a < nbTest; a++) {

                    Object object = Factory.createObject(type);

                    if (object instanceof FuzzyCounter)
                        ((FuzzyCounter) object).setN(nbCurrentThread);

                    FactoryFiller factoryFiller = new FactoryFiller(object, 1000);

                    Filler filler = factoryFiller.createFiller();
                    filler.fill();


                    List<Callable<Void>> callables = new ArrayList<>();
                    ExecutorService executor = Executors.newFixedThreadPool(nbCurrentThread);

                    CountDownLatch latch = new CountDownLatch(nbCurrentThread);
                    FactoryTester factoryTester = new FactoryTester(
                            object,
                            Arrays.stream(ratios).mapToInt(Integer::parseInt).toArray(),
                            latch
                    );

                    for (int j = 0; j < nbCurrentThread; j++) { // -1 if a thread only read.
                        Tester tester = factoryTester.createTester();
                        callables.add(tester);
                    }



//                   Code if one reader is needed

/*
                   FactoryTester factoryT = new FactoryTester(
                           object,
                           new int[] {0},
                           latch
                   );

                    Tester t = factoryT.createTester();
                    callables.add(t);
*/

//

                    ExecutorService executorCoordinator = Executors.newFixedThreadPool(1);
                    flag = new AtomicBoolean();
                    flag.set(true);
                    executorCoordinator.submit(new Coordinator());

                    List<Future<Void>> futures;

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
                }

                long timeTotal;
                double throughputADD, throughputREMOVE, throughputREAD, throughputTotal;

                timeTotal = timeAdd.get() + timeRemove.get() + timeRead.get();

                throughputADD = (nbAdd.get() / (double) timeTotal) * 1_000_000_000;
                throughputREMOVE = (nbRemove.get() / (double) timeTotal) * 1_000_000_000;
                throughputREAD = (nbRead.get() / (double) timeTotal) * 1_000_000_000;

                throughputTotal = throughputADD + throughputREMOVE + throughputREAD;

                if (_s){

                    if (nbCurrentThread == 1)
                        fileWriter = new FileWriter("results_"+type+"_ratio_write_old_"+ratios[0]+".txt", false);
                    else
                        fileWriter = new FileWriter("results_"+type+"_ratio_write_old_"+ratios[0]+".txt", true);

                    printWriter = new PrintWriter(fileWriter);
                    printWriter.println(nbCurrentThread + " " + throughputTotal);
                }

                if (_p){
                    System.out.println(nbCurrentThread + " " + throughputTotal); // printing the throughput per op for nbCurrentThread thread(s)
                    System.out.println("    -time/add : " + throughputADD);
                    System.out.println("    -time/remove : " + throughputREMOVE);
                    System.out.println("    -time/read: " + throughputREAD);
                    System.out.println("    -num read: " + nbRead.get());
                }

                nbCurrentThread *= 2;

                /*if(nbCurrentThread==2)
                    nbCurrentThread = nbThreads;
*/
                if (nbCurrentThread > nbThreads && nbCurrentThread != 2 * nbThreads) {
                    nbCurrentThread = nbThreads;
                }

                if(_p)
                    System.out.println();
                if (_s)
                    printWriter.close();
            }

        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        System.exit(0);
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
