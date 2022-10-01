package eu.cloudbutton.dobj.benchmark;

import eu.cloudbutton.dobj.benchmark.Tester.*;
import eu.cloudbutton.dobj.incrementonly.FuzzyCounter;
import eu.cloudbutton.dobj.Factory;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.ExplicitBooleanOptionHandler;
import org.kohsuke.args4j.spi.StringArrayOptionHandler;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.kohsuke.args4j.OptionHandlerFilter.ALL;

public class Microbenchmark {


    enum opType{
        ADD,
        REMOVE,
        READ;

    }
    public static AtomicLong timeAdd;
    public static AtomicLong timeRemove;
    public static AtomicLong timeRead;
    public static AtomicLong nbAdd;
    public static AtomicLong nbRemove;
    public static AtomicLong nbRead;
    public static AtomicLong nbAddFail;
    public static AtomicLong nbRemoveFail;
    public static AtomicLong nbReadFail;
    public static AtomicBoolean flag;
    public static boolean ratioFail;
    public static int nbCurrentThread;
    public static Map<Retwis.opType, AtomicLong> nbOperations;
    public static Map<Retwis.opType, AtomicLong> timeOperations;

    @Option(name = "-type", required = true, usage = "type to test")
    private String type;
    @Option(name = "-ratios", handler = StringArrayOptionHandler.class, usage = "ratios")
    private String[] ratios;
    @Option(name = "-nbThreads", usage = "Number of threads")
    private int nbThreads = Runtime.getRuntime().availableProcessors();
    @Option(name = "-time", usage = "How long will the test last (seconds)")
    private int time = 300;
    @Option(name = "-wTime", usage = "How long we wait till the test start (seconds)")
    private int wTime = 0;
    @Option(name = "-nbOps", usage = "Number of object initially added")
    private long nbOps = 1_000;
    @Option(name = "-nbTest", usage = "Number of test")
    private int nbTest = 1;
    @Option(name = "-s", handler = ExplicitBooleanOptionHandler.class, usage = "Save the result")
    private boolean _s = false;
    @Option(name = "-p", handler = ExplicitBooleanOptionHandler.class, usage = "Print the result")
    private boolean _p = false;
    @Option(name = "-ratioFail", handler = ExplicitBooleanOptionHandler.class, usage = "Compute the fail ratio")
    private boolean _ratioFail = false;
    @Option(name = "-asymmetric", handler = ExplicitBooleanOptionHandler.class, usage = "Asymmetric workload")
    private boolean _asymmetric = false;
    @Option(name = "-collisionKey", handler = ExplicitBooleanOptionHandler.class, usage = "Testing map with collision on key")
    public boolean _collisionKey = false;
    @Option(name = "-quickTest", handler = ExplicitBooleanOptionHandler.class, usage = "Testing only one and max nbThreads")
    public boolean _quickTest = false;

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        new Microbenchmark().doMain(args);
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

            if (ratios.length != 3)
                throw new java.lang.Error("Number of ratios must be 3 (% ADD, % REMOVE, % READ)");

            int total = 0;
            for (int ratio: Arrays.stream(ratios).mapToInt(Integer::parseInt).toArray()) {
                total += ratio;
            }

            if (total != 100){
                throw new java.lang.Error("Total ratio must be 100");
            }
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
            ratioFail = _ratioFail;

            PrintWriter printWriter = null;
            FileWriter fileWriter;
            Object object = null;

            nbCurrentThread = _asymmetric ? 2 : 1;

            for (;nbCurrentThread <= nbThreads; ) {
                System.out.println();
                nbOperations = new ConcurrentHashMap<>();
                timeOperations = new ConcurrentHashMap<>();

                for (Retwis.opType op : Retwis.opType.values()) {
                    nbOperations.put(op, new AtomicLong(0));
                    timeOperations.put(op, new AtomicLong(0));
                }

                for (int _nbTest = 0; _nbTest < nbTest; _nbTest++) {
                    if (_p)
                        System.out.println("Test numero : " + (_nbTest+1));

                    if (_nbTest == 0) {

                        object = Factory.createObject(type);

                        if (object instanceof FuzzyCounter)
                            ((FuzzyCounter) object).setN(nbCurrentThread);

                        FactoryFiller factoryFiller = new FactoryFiller(object, nbOps, _collisionKey);

                        if (_p)
                            System.out.println("* Start filling *");

                        Filler filler = factoryFiller.createFiller();

                        if (! type.contains("Sequential"))
                            filler.fill();

                        if (_p)
                            System.out.println("* End filling *");
                    }
//                    }

                    List<Callable<Void>> callables = new ArrayList<>();
                    ExecutorService executor = Executors.newFixedThreadPool(nbCurrentThread);

                    CountDownLatch latch = new CountDownLatch(nbCurrentThread);

                    FactoryTester factoryTester = new FactoryTesterBuilder()
                            .object(object)
                            .ratios(Arrays.stream(ratios).mapToInt(Integer::parseInt).toArray())
                            .latch(latch)
                            .useCollisionKey(_collisionKey)
                            .buildTester();

                    int nbComputingThread = _asymmetric ? nbCurrentThread - 1 : nbCurrentThread; // -1 if a specific thread perform a different operation.

                    for (int j = 0; j < nbComputingThread; j++) {
                        Tester tester = factoryTester.createTester();
                        callables.add(tester);
                    }

//                   Code if a specific thread perform a different operation.

                    if (_asymmetric){

                        FactoryTester factoryT = new FactoryTesterBuilder()
                                .object(object)
                                .ratios(new int[] {0, 100, 0}) // [add, remove, read]
                                .latch(latch)
                                .useCollisionKey(_collisionKey)
                                .buildTester();

                        Tester t = factoryT.createTester();
                        callables.add(t);
                    }

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



                long timeTotal = 0L, nbOpTotal = 0L;

                for (opType type: opType.values()){
                    timeTotal += timeOperations.get(type).get();
                    nbOpTotal += nbOperations.get(type).get();
                }

                double throughputTotal;

                throughputTotal = nbOpTotal/(double) (timeTotal) * 1_000_000_000;

                if (_s){

                    String nameFile = type + "_ALL.txt";

                    if (nbCurrentThread == 1 || (_asymmetric && nbCurrentThread == 2)) {
                        fileWriter = new FileWriter(nameFile, false);

                    }
                    else {
                        fileWriter = new FileWriter(nameFile + ".txt", true);
                    }
                    printWriter = new PrintWriter(fileWriter);
                    printWriter.println(nbCurrentThread + " " + throughputTotal);

                    if (_p){
                        for (int j = 0; j < 10; j++) System.out.print("-");
                        System.out.print(" Throughput total (op/s) : ");
                        System.out.println(String.format("%.3E", throughputTotal));
                    }

                    long nbOp, timeOp;

                    for (opType op: opType.values()){

                        nbOp = nbOperations.get(op).get();
                        timeOp = timeOperations.get(op).get();

                        nameFile = type + "_"+ op+".txt";

                        if (nbCurrentThread == 1 || (_asymmetric && nbCurrentThread == 2))
                            fileWriter = new FileWriter( nameFile, false);
                        else
                            fileWriter = new FileWriter(nameFile, true);

                        printWriter = new PrintWriter(fileWriter);
                        printWriter.println(nbCurrentThread +" "+  (nbOp / (double) timeOp) * 1_000_000_000);


                        if (_p){
                            for (int j = 0; j < 10; j++) System.out.print("-");
                            System.out.print(" Throughput (op/s) for "+op+" : ");
                            System.out.println(String.format("%.3E", (nbOp / (double) timeOp) * 1_000_000_000));
                        }

                        if (_s)
                            printWriter.flush();
                    }
                }

                nbCurrentThread *= 2;

                if (_quickTest){
                    if(nbCurrentThread==2 || (_asymmetric && nbCurrentThread == 4))
                        nbCurrentThread = nbThreads;
                }

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
        } catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }


    public class Coordinator implements Callable<Void> {

        public Coordinator(){}

        @Override
        public Void call() throws Exception {
            try {
                if (_p)
                    System.out.println("Warming up.");
                TimeUnit.SECONDS.sleep(wTime);
                flag.set(false);
                if (_p)
                    System.out.println("Computing.");
                TimeUnit.SECONDS.sleep(time);
                flag.set(true);
            } catch (InterruptedException e) {
                throw new Exception("Thread interrupted", e);
            }
            return null;
        }
    }

}
