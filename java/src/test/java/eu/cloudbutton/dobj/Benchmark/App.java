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
    private int _nbThreads = Runtime.getRuntime().availableProcessors();

    @Option(name = "-nbTest", usage = "Number of test")
    private int _nbTest = 1;

    @Option(name = "-nbOps", usage = "Number of operation done")
    private long _nbOps = 1_000_000;

    @Option(name = "-time", usage = "test time (seconds)")
    private int _time = 300;

    @Option(name = "-wTime", usage = "warming time (seconds)")
    private int _wTime = 0;

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

    @Option(name = "-quickTest", handler = ExplicitBooleanOptionHandler.class, usage = "Testing only one and max nbThreads")
    public boolean _quickTest = false;

    @Option(name = "-completionTime", handler = ExplicitBooleanOptionHandler.class, usage = "Computing the completion time")
    public boolean _completionTime = false;

    @Option(name = "-multipleOperation", handler = ExplicitBooleanOptionHandler.class, usage = "Computing operation multiples times")
    public boolean _multipleOperation = false;

    private AtomicBoolean flagComputing,flagWarmingUp;

    private Map<opType, AtomicInteger> nbOperations;
    private Map<opType, AtomicInteger> nbOperationsFailed;
    private Map<opType, AtomicLong> timeOperations;

    private Database database;

    int NB_USERS = 100000;

    int nbSign = 5;

    public static void main(String[] args) throws InterruptedException, IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        new App().doMain(args);
    }

    public void doMain(String[] args) throws InterruptedException, IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
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

        for (int nbCurrThread = 1; nbCurrThread <= _nbThreads;) {

            PrintWriter printWriter = null;
            FileWriter fileWriter;
            long startTime, endTime, timeTotal = 0L;

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

                for (int nbCurrTest = 1; nbCurrTest <= _nbTest; nbCurrTest++) {
                    List<Callable<Void>> callables = new ArrayList<>();
                    ExecutorService executor = Executors.newFixedThreadPool(nbCurrThread); // Additional count for the UserAdder
                    ExecutorService executorServiceCoordinator = Executors.newFixedThreadPool(1); // Coordinator

                    flagComputing = new AtomicBoolean(true);
                    flagWarmingUp = new AtomicBoolean(false);
                    database = new Database(typeMap, typeSet, typeQueue, typeCounter, alpha, nbCurrThread);

                    if (nbCurrThread == 1 && nbCurrTest == 1){
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



                    startTime = System.nanoTime();
                    futures = executor.invokeAll(callables);

                    try{
                        for (Future<Void> future : futures) {
                            future.get();
                        }
                    } catch (OutOfMemoryError | CancellationException | ExecutionException e) {
                        e.printStackTrace();
                        System.exit(0);
                    }

                    endTime = System.nanoTime();

                    timeTotal = endTime - startTime;

                    TimeUnit.SECONDS.sleep(5);
                    executor.shutdown();
                }

                long nbOpTotal = 0;
                long nbOpTotalFailed = 0;

                for (opType op : opType.values()){
                    nbOpTotal += nbOperations.get(op).get();
                    nbOpTotalFailed += nbOperationsFailed.get(op).get();
                }

                long nbOp;
//              long avgTimeTotal = timeTotal / nbCurrThread; // Compute the avg time to get the global throughput

                if (_s){

                    if (nbCurrThread == 1)
                        fileWriter = new FileWriter("retwis_ALL_operations.txt", false);
                    else
                        fileWriter = new FileWriter("retwis_ALL_operations.txt", true);

                    printWriter = new PrintWriter(fileWriter);
                    if (_completionTime)
                        printWriter.println(nbCurrThread +" "+ (_nbOps / (double) timeTotal) * 1_000_000_000);
                    else
                        printWriter.println(nbCurrThread +" "+ (nbOpTotal / (double) _time) * 1_000_000_000);
                }

                if (_p){
                    for (int j = 0; j < nbSign; j++) System.out.print("-");
                    System.out.print(" Completion time for " + _nbOps + " operations ");
                    for (int j = 0; j < nbSign; j++) System.out.print("-");
                    System.out.println();
                    System.out.println(" - "+ timeTotal/1_000_000_000 +" seconds");
                    System.out.println();
                    System.out.println("* Proportion of failed operations : " + (nbOpTotalFailed / (double) nbOpTotal) * 100);

                }

                if (_s)
                    printWriter.flush();

                if (! _completionTime){
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
                            System.out.println(" - "+String.format("%.3E", (nbOp / (double) timeTotal) * 1_000_000_000));
                            System.out.println("* Proportion of failed " + op + " operations : " + (nbOperationsFailed.get(op).get() / (double) nbOperations.get(op).get()) * 100);
                        }

                        if (_s)
                            printWriter.flush();
                    }
                }

                if(_p)
                    System.out.println();
                if (_s)
                    printWriter.close();

            }



            nbCurrThread *= 2;

            if (_quickTest){
                if(nbCurrThread==2)
                    nbCurrThread = _nbThreads;
            }

            if (nbCurrThread > _nbThreads && nbCurrThread != 2 * _nbThreads)
                nbCurrThread = _nbThreads;
        }
        System.exit(0);
    }

    public class RetwisApp implements Callable<Void>{

        protected final ThreadLocalRandom random;
        private final int[] ratiosArray;
        private final CountDownLatch latch;
        private final CountDownLatch latchFillDatabase;
        private ThreadLocal<Map<String, Queue<String>>> usersFollow; // Local map that associate to each user, the list of user that it follows
        private ThreadLocal<Integer> usersProbabilitySize = new ThreadLocal<>();
        private ThreadLocal<List<String>> arrayUsersFollow = new ThreadLocal<>(); // Local array that store the users handled by a thread
        private int nbRepeat = 1000;

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
                arrayUsersFollow.set(new ArrayList<>(usersFollow.get().keySet()));

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
                    for (int i = 0; i < nbRepeat; i++) {
                        compute(type);
                    }

                }

                for (int i = 0; i < _nbOps; i++) {
//                while (!flagComputing.get()){

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

                    long elapsedTime = 0L;

                    if (_multipleOperation){
                        for (int j = 0; j < nbRepeat; j++) {
                            elapsedTime += compute(type);
                        }

                        elapsedTime = elapsedTime / nbRepeat;
                    }else{
                        elapsedTime = compute(type);
                    }

                    if (elapsedTime != 0)
                        nbLocalOperations.compute(type, (key, value) -> value + 1);
                    long finalElapsedTime = elapsedTime;
                    timeLocalOperations.compute(type, (key, value) -> value + finalElapsedTime);
//                    if (elapsedTime == 0)
//                        nbLocalOperationsFailed.compute(type, (key, value) -> value + 1);
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

            int val = random.nextInt(arrayUsersFollow.get().size());

            userA = arrayUsersFollow.get().get(val);

            Queue<String> listFollow = usersFollow.get().get(userA);

            switch (type){
                case ADD:
                    if (_completionTime){
                        database.addUser();
                    }else{
                        startTime = System.nanoTime();
                        database.addUser();
                        endTime = System.nanoTime();
                    }


                    break;
                case FOLLOW:
                    n = random.nextInt(usersProbabilitySize.get()); // We choose a user to follow according to a probability
                    userB = database.getUsersProbability().get(n);

                    try{
                        if (!listFollow.contains(userB)){
                            if (_completionTime){
                                database.followUser(userA, userB);
                            }else {
                                startTime = System.nanoTime();
                                database.followUser(userA, userB);
                                endTime = System.nanoTime();
                            }
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
                            if (_completionTime){
                                database.unfollowUser(userA, userB);
                            }else{
                                startTime = System.nanoTime();
                                database.unfollowUser(userA, userB);
                                endTime = System.nanoTime();
                            }
                        }
                    }catch (NullPointerException e){
//                        System.out.println(userA + " may not have a list of follow (Unfollow method)");
                    }

                break;
                case TWEET:
                    String msg = "msg from " + userA;
                    if (_completionTime){
                        database.tweet(userA, msg);
                    }else{
                        startTime = System.nanoTime();
                        database.tweet(userA, msg);
                        endTime = System.nanoTime();
                    }

                break;
                case READ:
                    if (_completionTime){
                        database.showTimeline(userA);

                    }else{
                        startTime = System.nanoTime();
                        database.showTimeline(userA);
                        endTime = System.nanoTime();
                    }


                break;
                default:
                    throw new IllegalStateException("Unexpected value: " + type);
            }

            if (_completionTime)
                return -1;

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

                    TimeUnit.SECONDS.sleep(_wTime);

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
                if (! _completionTime)
                    flagComputing.set(true);
            } catch (InterruptedException e) {
                throw new Exception("Thread interrupted", e);
            }
            return null;
        }
    }
}
