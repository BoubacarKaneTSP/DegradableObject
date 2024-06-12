package eu.cloudbutton.dobj.benchmark;

import eu.cloudbutton.dobj.Factory;
import eu.cloudbutton.dobj.benchmark.tester.*;
import eu.cloudbutton.dobj.javaobj.ConcHashMap;
import eu.cloudbutton.dobj.segmented.ExtendedSegmentedHashMap;
import eu.cloudbutton.dobj.types.Counter;
import eu.cloudbutton.dobj.incrementonly.FuzzyCounter;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.ExplicitBooleanOptionHandler;
import org.kohsuke.args4j.spi.StringArrayOptionHandler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static org.kohsuke.args4j.OptionHandlerFilter.ALL;

public class Microbenchmark {

    public enum opType{
        ADD,
        REMOVE,
        READ
    }
    public static AtomicBoolean flag;
    public static boolean ratioFail;
    public static int nbCurrentThread;
    public static List<AtomicLong> nbOperations;
    public static List<AtomicLong> timeOperations;


    @Option(name = "-type", required = true, usage = "type to test")
    private String type;
    @Option(name = "-ratios", handler = StringArrayOptionHandler.class, usage = "ratios")
    private String[] ratios = {"50","50","0"};
    @Option(name = "-nbThreads", usage = "Number of threads")
    private int nbThreads = Runtime.getRuntime().availableProcessors();
    @Option(name = "-time", usage = "How long will the test last (seconds)")
    private int time = 300;
    @Option(name = "-wTime", usage = "How long we wait till the test start (seconds)")
    private int wTime = 0;
    @Option(name = "-nbOps", usage = "Number of object initially added") // FIXME
    private long nbOps = 1_000;
    @Option(name = "-nbItems", usage = "Number of items max per thread")
    private int _nbItems = 1_000_000;
    @Option(name = "-nbTest", usage = "Number of test")
    private int _nbTest = 1;
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
    @Option(name = "-quickTest", handler = ExplicitBooleanOptionHandler.class, usage = "Only test max nbThreads")
    public boolean _quickTest = false;
    @Option(name = "-gcinfo", handler = ExplicitBooleanOptionHandler.class, usage = "Compute gc info")
    public boolean _gcinfo = false;
    @Option(name = "-D", handler = ExplicitBooleanOptionHandler.class, usage = "delete old results")
    private boolean _delte = false;
    public static Map<String, Integer> map = new ConcurrentHashMap<>();

    public static void main(String[] args) throws ExecutionException, InterruptedException, NoSuchFieldException {
//        Key key1 = new ThreadLocalKey(10L, 10L);
//        Long long1 = 0L;
//        System.out.println(ClassLayout.parseClass(long1.getClass()).toPrintable());
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

            if (ratios==null || ratios.length != 3)
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
            System.err.println("  Example: java eu.cloudbutton.dobj.benchmark.Microbenchmark" + parser.printExample(ALL));

            return;
        }

        try{
            ratioFail = _ratioFail;

            PrintWriter printWriter = null;
            FileWriter fileWriter = null;
            Object object;
            long startTime, endTime, benchmarkAvgTime;

            if (_asymmetric && nbThreads == 1)
                nbThreads = 2;

            nbCurrentThread = nbThreads;

            if(_quickTest)
                nbCurrentThread = nbThreads;

            while (nbCurrentThread <= nbThreads) {
                if (_gcinfo)
                    System.out.println("nbThread : "+nbCurrentThread);

                if (_p) {
                    System.out.println();
                    System.out.println("Nb threads = " + nbCurrentThread);
                }

                for (int _nbTest = 0; _nbTest < 1; _nbTest++) {
                    nbOperations = new CopyOnWriteArrayList<>();
                    timeOperations = new CopyOnWriteArrayList<>();
                    long size = 0;
                    benchmarkAvgTime = 0L;

                    for (opType ignored : opType.values()) {
                        nbOperations.add(new AtomicLong(0));
                        timeOperations.add(new AtomicLong(0));
                    }

                    if (_p)
                        System.out.println("Test #" + (_nbTest+1));

                    List<Callable<Void>> callables = new ArrayList<>();
                     ExecutorService executor = Executors.newFixedThreadPool(nbCurrentThread);
//                    ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

                    object = Factory.newObject(type);

                    if (object instanceof FuzzyCounter)
                        ((FuzzyCounter) object).setN(nbCurrentThread + 1); // +1 for the thread that fill the object

                    FactoryFiller factoryFiller = new FactoryFiller(object,nbCurrentThread, nbOps, _collisionKey, _nbItems);

                    Filler filler = factoryFiller.createFiller();

                    if (_p)
                        System.out.println("=> Start filling <=");

                    if (!type.contains("Sequential"))
                        filler.fill();
                    else{
                        for (int i = 0; i < nbOps; i++) {
                            ((Queue)object).add(i);
                        }
                    }

                    if (_p)
                        System.out.println("=> End filling <=");

                    CountDownLatch latch = new CountDownLatch(nbCurrentThread + 1);

                    FactoryTester factoryTester = new FactoryTesterBuilder()
                            .object(object)
                            .ratios(Arrays.stream(ratios).mapToInt(Integer::parseInt).toArray())
                            .latch(latch)
                            .useCollisionKey(_collisionKey)
                            .maxItemPerThread(_nbItems)
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

                    ExecutorService executorCoordinator = Executors.newFixedThreadPool(1);
                    flag = new AtomicBoolean();
                    flag.set(true);
                    executorCoordinator.submit(new Coordinator(latch));

                    List<Future<Void>> futures;

                    // launch computation
                    if (_gcinfo){
                        System.out.println("Start benchmark");
                    }
                    startTime = System.nanoTime();
                    futures = executor.invokeAll(callables);
                    try{
                        for (Future<Void> future : futures) {
                            future.get();
                        }
                    } catch (CancellationException e) {
                        //ignore
                        System.out.println(e);
                    }
                    endTime = System.nanoTime();
                    if (_gcinfo) {
                        System.out.println("End benchmark");
                    }

                    if (object instanceof ExtendedSegmentedHashMap){
                        System.out.println("Nb tree bin : " + ((ExtendedSegmentedHashMap) object).getNbTreeBin());
                        System.out.println("Nb bin : " + ((ExtendedSegmentedHashMap) object).getNbBin());
                        System.out.println("Avg size bin : " + ((ExtendedSegmentedHashMap) object).getAvgSizeBin());
                        System.out.println("tab size: " + ((ExtendedSegmentedHashMap) object).getTabSize());
                    } else if (object instanceof ConcHashMap) {
                        System.out.println("Nb tree bin : " + ((ConcHashMap) object).getNbTreeBin());
                        System.out.println("Nb bin : " + ((ConcHashMap) object).getNbBin());
                        System.out.println("Avg size bin : " + ((ConcHashMap) object).getAvgSizeBin());
                        System.out.println("tab size: " + ((ConcHashMap) object).getTabSize());
                    }


                    benchmarkAvgTime += endTime - startTime;

                    if (object instanceof Map) {
                        size+=((Map<?, ?>) object).size();
                    }
                    else if (object instanceof Set) {
                        size+=((Set<?>) object).size();
                    }
                    else if (object instanceof Queue) {
                        size+=((Queue<?>) object).size();
                    }
                    else if (object instanceof Counter) {
                        size+=((Counter) object).get();
                    }
                    executor.shutdownNow();

                    String directory = "microbenchmark_results";

                    File subdirectory = new File(directory);

                    if (!subdirectory.exists()) {
                        subdirectory.mkdirs();
                    }

                    if (_gcinfo) {
                        System.out.println("benchmarkAvgTime : " + (benchmarkAvgTime / 1_000_000) + " in test : " + _nbTest);
                    }
                    if (_p)
                        System.out.println("End test num : " + _nbTest);

                    long timeTotal = 0L, nbOpTotal = 0L;

                    int opNumber = 0;

                    for (opType ignored : opType.values()){
                        timeTotal += timeOperations.get(opNumber).get();
                        nbOpTotal += nbOperations.get(opNumber).get();
                        opNumber++;
                    }

                    double throughputTotal;

                    throughputTotal = (nbOpTotal/(double) (timeTotal)) * nbCurrentThread * 1_000_000_000;

                    if (_s){
                        String nameFile = object.getClass().getSimpleName() + "_ALL.txt";
                        fileWriter = new FileWriter(directory + File.separator + nameFile, true);

//                    if (nbCurrentThread == 1 || (_asymmetric && nbCurrentThread == 2))
//                        fileWriter = new FileWriter(nameFile, false);
//                    else
//                        fileWriter = new FileWriter(nameFile, true);
                        printWriter = new PrintWriter(fileWriter);

                        printWriter.println(nbCurrentThread + " " + throughputTotal);

                        printWriter.close();
                        fileWriter.close();
                    }

                    if (_p){
                        for (int j = 0; j < 10; j++) System.out.print("-");
                        System.out.print(" Throughput total (op/s) : ");
                        System.out.printf("%.3E%n", throughputTotal);
                    }

                    long nbOp, timeOp;

                    opNumber = 0;
                    for (opType op: opType.values()) {

                        nbOp = nbOperations.get(opNumber).get();
                        timeOp = timeOperations.get(opNumber).get();
                        opNumber++;
                        if (_s) {

                            String nameOpFile = object.getClass().getSimpleName() + "_" + op + ".txt";
                            fileWriter = new FileWriter(directory + File.separator + nameOpFile, true);

                            printWriter = new PrintWriter(fileWriter);
                            printWriter.println(nbCurrentThread + " " + ((nbOp / (double) timeOp)*nbCurrentThread) * 1_000_000_000);
                            printWriter.close();
                            fileWriter.close();
                        }

                        if (_p) {
                            for (int j = 0; j < 10; j++) System.out.print("-");
                            System.out.print("Throughput (op/s) for " + op + " : ");
                            System.out.printf("%.3E%n", ((nbOp / (double) timeOp)*nbCurrentThread) * 1_000_000_000);
                        }
                    }

                    if (_p)
                        System.out.println("Object's size at the end of benchmark : " + size);
                    if (_s){
                        String nameSizeFile = type + "_size.txt";

                        fileWriter = new FileWriter(directory + File.separator + nameSizeFile, true);

                        printWriter = new PrintWriter(fileWriter);
                        printWriter.println(nbCurrentThread + " " + size);
                        printWriter.close();
                        fileWriter.close();
                    }
                }

                nbCurrentThread *= 2;

//                if (_quickTest){
//                    if(nbCurrentThread==2 || (_asymmetric && nbCurrentThread == 4))
//                        nbCurrentThread = nbThreads;
//                }

                if (nbCurrentThread > nbThreads && nbCurrentThread != 2 * nbThreads) {
                    nbCurrentThread = nbThreads;
                }

                if(_p)
                    System.out.println();
                if (_s) {
                    assert printWriter != null;
                    assert fileWriter != null;
                    printWriter.close();
                    fileWriter.close();
                }
            }

            System.exit(0);
            } catch (IOException | ClassNotFoundException exception) {
            exception.printStackTrace();
        }

    }

    public class Coordinator implements Callable<Void> {

        CountDownLatch latch;
        public Coordinator(CountDownLatch latch){
            this.latch = latch;
        }

        @Override
        public Void call() throws Exception {
            try {
                if (_p)
                    System.out.println("Warming up.");
                TimeUnit.SECONDS.sleep(wTime);
                flag.set(false);
                latch.countDown();
                latch.await();
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
