package eu.cloudbutton.dobj.Benchmark;

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

import static org.kohsuke.args4j.OptionHandlerFilter.ALL;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class App {

    enum opType{
        ADD,
        FOLLOW,
        UNFOLLOW,
        TWEET,
        READ
    }

    @Option(name="-set", required = true, usage = "type of Set")
    private String typeSet;

    @Option(name="-queue", required = true, usage = "type of Queue")
    private String typeQueue;

    @Option(name="-counter", required = true, usage = "type of Counter")
    private String typeCounter;

    @Option(name="-map", required = true, usage = "type of Map")
    private String typeMap;

    @Option(name = "-distribution", required = true, handler = StringArrayOptionHandler.class, usage = "distribution")
    private String[] distribution;

    @Option(name = "-nbThreads", usage = "Number of threads")
    private int nbThreads = Runtime.getRuntime().availableProcessors() / 2;

    @Option(name = "-nbTest", usage = "Number of test")
    private int nbTest = 1;

    @Option(name = "-time", usage = "test time (seconds)")
    private int time = 300;

    @Option(name = "-wTime", usage = "warming time (seconds)")
    private int wTime = 0;

    @Option(name = "-alphaInit", usage = "first value tested for alpha (powerlaw settings)")
    private double _alphaInit = 1.315;

    @Option(name = "-alphaMin", usage = "min value tested for alpha (powerlaw settings)")
    private double _alphaMin = 1.315;

    @Option(name = "-alphaStep", usage = "step between two value tested for alpha (powerlaw settings)")
    private double _alphaStep = 0.05;

    @Option(name = "-s", handler = ExplicitBooleanOptionHandler.class, usage = "Save the result")
    private boolean _s = false;

    @Option(name = "-p", handler = ExplicitBooleanOptionHandler.class, usage = "Print the result")
    private boolean _p = false;

    private AtomicBoolean flagComputing,flagWarmingUp;

    private Map<opType, AtomicInteger> nbOperations;
    private Map<opType, AtomicInteger> nbOperationsFailed;
    private Map<opType, AtomicLong> timeOperations;

    private Database database;

    int NB_USERS = 100000;

    int nbSign = 5;

    public static void main(String[] args) throws InterruptedException, IOException, ClassNotFoundException {
        new App().doMain(args);
    }

    public void doMain(String[] args) throws InterruptedException, IOException, ClassNotFoundException {
        CmdLineParser parser = new CmdLineParser(this);

        try{
            parser.parseArgument(args);

            if (args.length < 1)
                throw new CmdLineException(parser, "No argument is given");

            if (distribution.length != 4){
                throw new java.lang.Error("Number of ratios must be 4 (% add, % follow or unfollow, % tweet, % read)");
            }

            int total = 0;
            for (int ratio: Arrays.stream(distribution).mapToInt(Integer::parseInt).toArray()) {
                total += ratio;
            }

            if (total != 100){
                throw new java.lang.Error("Total ratio must be 100");
            }

        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            System.err.println("java SampleMain [options...] arguments...");
            // print the list of available options
            parser.printUsage(System.err);
            System.err.println();

            // print option sample. This is useful some time
            System.err.println("  Example: java eu.cloudbutton.dobj.Benchmark.App" + parser.printExample(ALL));

            return;
        }

        if (_p)
            System.out.println("Launching test from App.java, a clone of Retwis...");

        List<Double> listAlpha = new ArrayList<>();

        for (double i = _alphaInit ; i >= _alphaMin; i-=_alphaStep) {
            listAlpha.add(i);
        }

        for (int nbCurrThread = 1; nbCurrThread <= nbThreads;) {

            PrintWriter printWriter = null;
            FileWriter fileWriter;

            if (_p){
                System.out.println();
                for (int j = 0; j < 2*nbSign; j++) System.out.print("*");
                System.out.print( " Results for ["+nbCurrThread+"] threads ");
                for (int j = 0; j < 2*nbSign; j++) System.out.print("*");
                System.out.println();
            }

            for (double alpha : listAlpha) {
                if (_p){
                    System.out.println();
                    for (int j = 0; j < 2*nbSign; j++) System.out.print("-");
                    System.out.print( " Results for alpha = ["+alpha+"] ");
                    for (int j = 0; j < 2*nbSign; j++) System.out.print("-");
                    System.out.println();
                }

                for (int nbCurrTest = 1; nbCurrTest <= nbTest; nbCurrTest++) {
                    List<Callable<Void>> callables = new ArrayList<>();
                    ExecutorService executor = Executors.newFixedThreadPool(nbCurrThread); // Additional count for the UserAdder
                    ExecutorService executorServiceCoordinator = Executors.newFixedThreadPool(1); // Coordinator

                    flagComputing = new AtomicBoolean(true);
                    flagWarmingUp = new AtomicBoolean(false);
                    database = new Database(typeMap, typeSet, typeQueue, typeCounter, alpha, nbCurrThread);

                    if (nbCurrThread == 1){
                        flagWarmingUp.set(true);
                    }

                    if (nbCurrTest == 1) {
                        nbOperations = new ConcurrentHashMap<>();
                        nbOperationsFailed = new ConcurrentHashMap<>();
                        timeOperations = new ConcurrentHashMap<>();

                        for (opType op : opType.values()) {
                            nbOperations.put(op, new AtomicInteger(0));
                            nbOperationsFailed.put(op, new AtomicInteger(0));
                            timeOperations.put(op, new AtomicLong(0));
                        }
                    }

                    CountDownLatch latch = new CountDownLatch(nbCurrThread+1); // Additional counts for the coordinator
                    CountDownLatch latchFillDatabase = new CountDownLatch(nbCurrThread);

                    for (int j = 0; j < nbCurrThread; j++) {
                        RetwisApp retwisApp = new RetwisApp(
                                latch,
                                latchFillDatabase
                        );
                        callables.add(retwisApp);
                    }

                    executorServiceCoordinator.submit(new Coordinator(latch));

                    List<Future<Void>> futures;
                    futures = executor.invokeAll(callables);

                    try{
                        for (Future<Void> future : futures) {
                            future.get();
                        }
                    } catch (OutOfMemoryError | CancellationException | ExecutionException e) {
                        e.printStackTrace();
                        System.exit(0);
                    }

                    TimeUnit.SECONDS.sleep(5);
                    executor.shutdown();
                }

                long nbOpTotal = 0;
                long nbOpTotalFailed = 0;
                long timeTotal = 0L;

                for (opType op : opType.values()){
                    nbOpTotal += nbOperations.get(op).get();
                    nbOpTotalFailed += nbOperationsFailed.get(op).get();
                    timeTotal += timeOperations.get(op).get();
                }

                long nbOp = nbOpTotal - nbOpTotalFailed;
//              long avgTimeTotal = timeTotal / nbCurrThread; // Compute the avg time to get the global throughput

                if (_s){

                    if (nbCurrThread == 1)
                        fileWriter = new FileWriter("retwis_ALL_operations.txt", false);
                    else
                        fileWriter = new FileWriter("retwis_ALL_operations.txt", true);

                    printWriter = new PrintWriter(fileWriter);
                    printWriter.println(nbCurrThread +" "+ (nbOp / (double) timeTotal) * 1_000_000_000);
                }

                if (_p){
                    for (int j = 0; j < nbSign; j++) System.out.print("-");
                    System.out.print(" Throughput for all type of operations ");
                    for (int j = 0; j < nbSign; j++) System.out.print("-");
                    System.out.println();
                    System.out.println(" - "+ (nbOp / (double) timeTotal) * 1_000_000_000);
                    System.out.println("* Proportion of failed operations : " + (nbOpTotalFailed / (double) nbOpTotal) * 100);

                }

                if (_s)
                    printWriter.flush();

                for (opType op: opType.values()){

                    nbOp = nbOperations.get(op).get() - nbOperationsFailed.get(op).get();

//                    timeOperations.get(op).set( timeOperations.get(op).get()/nbCurrThread );  // Compute the avg time to get the global throughput

                    if (_s){
                        if (nbCurrThread == 1)
                            fileWriter = new FileWriter("retwis_"+op+"_operations.txt", false);
                        else
                            fileWriter = new FileWriter("retwis_"+op+"_operations.txt", true);
                        printWriter = new PrintWriter(fileWriter);
                        printWriter.println(nbCurrThread +" "+  (nbOp / (double) timeTotal) * 1_000_000_000);
                    }

                    if (_p){
                        for (int j = 0; j < nbSign; j++) System.out.print("-");
                        System.out.print(" Throughput for "+op+" operations ");
                        for (int j = 0; j < nbSign; j++) System.out.print("-");
                        System.out.println();
                        System.out.println(" - "+ (nbOp / (double) timeTotal) * 1_000_000_000);
                        System.out.println("* Proportion of failed " + op + " operations : " + (nbOperationsFailed.get(op).get() / (double) nbOperations.get(op).get()) * 100);
                    }

                    if (_s)
                        printWriter.flush();
                }
                if(_p)
                    System.out.println();
                if (_s)
                    printWriter.close();

            }



            nbCurrThread *= 2;
            if (nbCurrThread > nbThreads && nbCurrThread != 2 * nbThreads)
                nbCurrThread = nbThreads;
        }
        System.exit(0);
    }

    public class RetwisApp implements Callable<Void>{

        protected final ThreadLocalRandom random;
        private final int[] ratiosArray;
        private final CountDownLatch latch;
        private final CountDownLatch latchFillDatabase;
        private ThreadLocal<Map<String, Queue<String>>> usersFollow;
        private ThreadLocal<Integer> usersProbabilitySize = new ThreadLocal<>();

        public RetwisApp(CountDownLatch latch,CountDownLatch latchFillDatabase) {
            this.random = ThreadLocalRandom.current();
            this.ratiosArray = Arrays.stream(distribution).mapToInt(Integer::parseInt).toArray();
            this.latch = latch;
            this.latchFillDatabase = latchFillDatabase;
            usersFollow = ThreadLocal.withInitial(() -> new HashMap<>());
        }

        @Override
        public Void call(){

            try{

                opType type;
                int val;

                AbstractMap<opType, Integer> nbLocalOperations = new HashMap<>();
                AbstractMap<opType, Integer> nbLocalOperationsFailed = new HashMap<>();
                AbstractMap<opType, Long> timeLocalOperations = new HashMap<>();

                for (opType op: opType.values()){
                    nbLocalOperations.put(op, 0);
                    nbLocalOperationsFailed.put(op, 0);
                    timeLocalOperations.put(op, 0L);
                }

                database.fill(NB_USERS, latchFillDatabase, usersFollow);

                latch.countDown();

                latch.await();

                usersProbabilitySize.set(database.getUsersProbability().size());

                while (flagComputing.get()) { // warm up

                    val = random.nextInt(100);

                    if (val < ratiosArray[0]) { // add
                        type = opType.ADD;
                    } else if (val >= ratiosArray[0] && val < ratiosArray[0] + ratiosArray[1]) { //follow or unfollow
                        if (val % 2 == 0) { // follow
                            type = opType.FOLLOW;
                        } else { // unfollow
                            type = opType.UNFOLLOW;
                        }
                    } else if (val >= ratiosArray[0] + ratiosArray[1] && val < ratiosArray[0] + ratiosArray[1] + ratiosArray[2]) { // tweet
                        type = opType.TWEET;
                    } else { // read
                        type = opType.READ;
                    }
                    compute(type);
                }

                while (!flagComputing.get()){

                    val = random.nextInt(100);

                    if(val < ratiosArray[0]){ // add
                        type = opType.ADD;
                    }else if (val >= ratiosArray[0] && val < ratiosArray[0]+ ratiosArray[1]){ //follow or unfollow
                        if (val%2 == 0){ //follow
                            type = opType.FOLLOW;
                        }else{ //unfollow
                            type = opType.UNFOLLOW;
                        }
                    }else if (val >= ratiosArray[0]+ ratiosArray[1] && val < ratiosArray[0]+ ratiosArray[1]+ ratiosArray[2]){ //tweet
                        type = opType.TWEET;
                    }else{ //read
                        type = opType.READ;
                    }

                    long elapsedTime = compute(type);

                    nbLocalOperations.compute(type, (key, value) -> value + 1);
                    timeLocalOperations.compute(type, (key, value) -> value + elapsedTime);
                    if (elapsedTime == 0)
                        nbLocalOperationsFailed.compute(type, (key, value) -> value + 1);
                }

                for (opType op: opType.values()){
                    nbOperations.get(op).addAndGet(nbLocalOperations.get(op));
                    nbOperationsFailed.get(op).addAndGet(nbLocalOperationsFailed.get(op));
                    timeOperations.get(op).addAndGet(timeLocalOperations.get(op));
                }

            } catch (InterruptedException | NoSuchMethodException | InvocationTargetException | IllegalAccessException | ClassNotFoundException | InstantiationException e) {
                e.printStackTrace();
            }
            return null;
        }

        public void dummyFunction() throws InterruptedException {
            TimeUnit.MICROSECONDS.sleep(1000);
        }

        public long compute(opType type) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, ClassNotFoundException, InstantiationException, InterruptedException {

            long startTime = 0L, endTime= 0L;
            int n;
            String userA = "", userB;

/*            int i = 0, val = random.nextInt(usersFollow.get().size());

            for (String s: usersFollow.get().keySet()){
                if (i == val)
                    userA = s;

                i++;
            }*/

            Queue<String> listFollow = usersFollow.get().get(userA);

            switch (type){
                case ADD:
                    startTime = System.nanoTime();
                    database.addUser();
                    endTime = System.nanoTime();

                    break;
                case FOLLOW:
                    n = random.nextInt(usersProbabilitySize.get());
                    userB = database.getUsersProbability().get(n);

                    try{
                        if (!listFollow.contains(userB)){
                            startTime = System.nanoTime();
                            database.followUser(userA, userB);
                            endTime = System.nanoTime();
                            listFollow.add(userB);
                        }
                    }catch (NullPointerException e){
//                        System.out.println(userA + " may not have a list of follow (Follow method)");
//                        Make a "debug mode" to specify when a process doesn't handle userA
                    }

                break;
                case UNFOLLOW:
                    try{
                        userB = listFollow.poll();
                        if (userB != null){
                            startTime = System.nanoTime();
                            database.unfollowUser(userA, userB);
                            endTime = System.nanoTime();
                        }
                    }catch (NullPointerException e){
//                        System.out.println(userA + " may not have a list of follow (Unfollow method)");
                    }

                break;
                case TWEET:
                    String msg = "msg from " + userA;
                    startTime = System.nanoTime();
                    database.tweet(userA, msg);
                    endTime = System.nanoTime();

                break;
                case READ:
                    startTime = System.nanoTime();
                    database.showTimeline(userA);
                    endTime = System.nanoTime();

                break;
                default:
                    throw new IllegalStateException("Unexpected value: " + type);
            }

            return endTime - startTime;
        }
    }

    public class Coordinator implements Callable<Void> {

        private final CountDownLatch latch;

        public Coordinator(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public Void call() throws Exception {
            try {

                if (flagWarmingUp.get()){
                    if (_p)
                        System.out.println("Filling the database");

                    latch.countDown();
                    latch.await();

                    if (_p){
                        System.out.println("Warming up");
                    }

                    TimeUnit.SECONDS.sleep(wTime);

                    flagComputing.set(false);
                }
                else{
                    flagComputing.set(false);
                    latch.countDown();
                    latch.await();
                }

                if (_p){
                    System.out.println("Computing");
                }
                TimeUnit.SECONDS.sleep(time);
                flagComputing.set(true);
            } catch (InterruptedException e) {
                throw new Exception("Thread interrupted", e);
            }
            return null;
        }
    }
}
