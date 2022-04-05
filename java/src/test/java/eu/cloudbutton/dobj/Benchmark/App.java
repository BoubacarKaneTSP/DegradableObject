package eu.cloudbutton.dobj.Benchmark;

import eu.cloudbutton.dobj.types.*;
import nl.peterbloem.powerlaws.DiscreteApproximate;
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
import java.util.AbstractSet;
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

    @Option(name = "-ratios", required = true, handler = StringArrayOptionHandler.class, usage = "ratios")
    private String[] ratios;

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

    private AtomicBoolean flag;

    private Map<opType, AtomicInteger> nbOperations;
    private Map<opType, AtomicLong> timeOperations;

    int nbSign = 5;

    public static void main(String[] args) throws InterruptedException, IOException {
        new App().doMain(args);
    }

    public void doMain(String[] args) throws InterruptedException, IOException {
        CmdLineParser parser = new CmdLineParser(this);

        try{
            parser.parseArgument(args);

            if (args.length < 1)
                throw new CmdLineException(parser, "No argument is given");

            if (ratios.length != 4){
                throw new java.lang.Error("Number of ratios must be 4 (% add, % follow or unfollow, % tweet, % read)");
            }

            int total = 0;
            for (int ratio: Arrays.stream(ratios).mapToInt(Integer::parseInt).toArray()) {
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
                    java.util.List<Callable<Void>> callables = new ArrayList<>();
                    ExecutorService executor = Executors.newFixedThreadPool(nbCurrThread);

                    if (nbCurrTest == 1) {
                        nbOperations = new ConcurrentHashMap<>();
                        timeOperations = new ConcurrentHashMap<>();

                        for (opType op : opType.values()) {
                            nbOperations.put(op, new AtomicInteger(0));
                            timeOperations.put(op, new AtomicLong(0));
                        }
                    }

                    CountDownLatch latch = new CountDownLatch(nbCurrThread+1); // Additional count for the coordinator
                    CountDownLatch latchFillDatabase = new CountDownLatch(nbCurrThread);

                    for (int j = 0; j < nbCurrThread; j++) {
                        RetwisApp retwisApp = new RetwisApp(
                                alpha,
                                latch,
                                latchFillDatabase,
                                nbCurrThread);
                        callables.add(retwisApp);
                    }

                    ExecutorService executorService = Executors.newFixedThreadPool(1);
                    flag = new AtomicBoolean(true);

                    executorService.submit(new Coordinator(latch));

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
                long avgTimeTotal = 0L;

                for (opType op : opType.values()){
                    nbOpTotal += nbOperations.get(op).get();
                    avgTimeTotal += timeOperations.get(op).get();
                }

//                avgTimeTotal = avgTimeTotal / nbCurrThread; // Compute the avg time to get the global throughput

                if (_s){

                    if (nbCurrThread == 1)
                        fileWriter = new FileWriter("retwis_all_operations.txt", false);
                    else
                        fileWriter = new FileWriter("retwis_all_operations.txt", true);

                    printWriter = new PrintWriter(fileWriter);
                    printWriter.println(nbCurrThread +" "+ (nbOpTotal / (double) avgTimeTotal) * 1_000_000_000);
                }

                if (_p){
                    for (int j = 0; j < nbSign; j++) System.out.print("-");
                    System.out.print(" Throughput for all type of operations ");
                    for (int j = 0; j < nbSign; j++) System.out.print("-");
                    System.out.println();
                    System.out.println(" - "+ (nbOpTotal / (double) avgTimeTotal) * 1_000_000_000);

                }

                if (_s)
                    printWriter.flush();

                for (opType op: opType.values()){

//                    timeOperations.get(op).set( timeOperations.get(op).get()/nbCurrThread );  // Compute the avg time to get the global throughput

                    if (_s){
                        if (nbCurrThread == 1)
                            fileWriter = new FileWriter("retwis_"+op+"_operations.txt", false);
                        else
                            fileWriter = new FileWriter("retwis_"+op+"_operations.txt", true);
                        printWriter = new PrintWriter(fileWriter);
                        printWriter.println(nbCurrThread +" "+  (nbOperations.get(op).get() / (double) timeOperations.get(op).get()) * 1_000_000_000);
                    }

                    if (_p){
                        for (int j = 0; j < nbSign; j++) System.out.print("-");
                        System.out.print(" Throughput for "+op+" operations ");
                        for (int j = 0; j < nbSign; j++) System.out.print("-");
                        System.out.println();
                        System.out.println(" - "+ (nbOperations.get(op).get() / (double) timeOperations.get(op).get()) * 1_000_000_000);
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

        private  final int NB_USERS = 500000; //Number of users initially added to the database
        protected int USER_PER_THREAD;
        protected final ThreadLocalRandom random;
        private final String methodSet;
        private final String methodQueue;
        private final String methodCounter;
        private final int[] ratiosArray;
        private final double alpha;
        private final CountDownLatch latch;
        private final CountDownLatch latchFillDatabase;
        private final Factory factory;
        private AbstractMap<String, AbstractSet<String>> mapFollowers;
        private AbstractMap<String, Timeline> mapTimelines;
//        private Map<String, AbstractCounter> nbFollower;


        public RetwisApp(double alpha, CountDownLatch latch,CountDownLatch latchFillDatabase, int nbThread) {
            this.random = ThreadLocalRandom.current();
            this.methodSet = "create" + typeSet;
            this.methodQueue =  "create" + typeQueue;
            this.methodCounter =  "create" + typeCounter;
            this.ratiosArray = Arrays.stream(ratios).mapToInt(Integer::parseInt).toArray();
            this.alpha = alpha;
            this.latch = latch;
            this.latchFillDatabase = latchFillDatabase;
            this.factory = new Factory();
            this.USER_PER_THREAD = NB_USERS / nbThread;
            this.mapFollowers = new ConcurrentHashMap<>();
            this.mapTimelines = new ConcurrentHashMap<>();
//            this.nbFollower = new ConcurrentHashMap<>();
        }

        @Override
        public Void call(){

            opType type;
            int val;

            AbstractMap<opType, Integer> nbLocalOperations = new HashMap<>();
            AbstractMap<opType, Long> timeLocalOperations = new HashMap<>();

            for (opType op: opType.values()){
                nbLocalOperations.put(op, 0);
                timeLocalOperations.put(op, 0L);
            }

            try{

                fill_database();

                latch.countDown();

                latch.await();


                while (flag.get()){     // warm up

                    val = random.nextInt(100);

                    if(val < ratiosArray[0]){ // add
                        type = opType.ADD;
                    }else if (val >= ratiosArray[0] && val < ratiosArray[0]+ ratiosArray[1]){ //follow or unfollow
                        if (val%2 == 0){ // follow
                            type = opType.FOLLOW;
                        }else{ // unfollow
                            type = opType.UNFOLLOW;
                        }
                    }else if (val >= ratiosArray[0]+ ratiosArray[1] && val < ratiosArray[0]+ ratiosArray[1]+ ratiosArray[2]){ // tweet
                        type = opType.TWEET;
                    }else{ // read
                        type = opType.READ;
                    }

                    compute(type);
                }

                while (!flag.get()){

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
                }

                for (opType op: opType.values()){
                    nbOperations.get(op).addAndGet(nbLocalOperations.get(op));
                    timeOperations.get(op).addAndGet(timeLocalOperations.get(op));
                }

            } catch (InterruptedException | NoSuchMethodException | InvocationTargetException | IllegalAccessException | ClassNotFoundException | InstantiationException e) {
                e.printStackTrace();
            }
            return null;
        }

        public void dummyFunction() throws InterruptedException {
            TimeUnit.MICROSECONDS.sleep(10000);
        }

        public void fill_database() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, InterruptedException, ClassNotFoundException, InstantiationException {

            int n;
            String userA;
            String userB;

            //adding users
            for (int id = 0; id < USER_PER_THREAD; id++) {
                addUser("user_"+Thread.currentThread().getName()+"_"+id);
            }

            latchFillDatabase.countDown();

            latchFillDatabase.await();

            String[] users = mapFollowers.keySet().toArray(new String[NB_USERS]);

            int bound = 1000;
            
            List<Integer> data = new DiscreteApproximate(1, alpha).generate(bound);

            int i = 0, max = (100000 * NB_USERS) / 175000000; //10⁵ is ~ the number of follow max on twitter and 175000000 is the number of user on twitter (stats from the article)

            for (int val: data){
                if (val >= max) {
                    data.set(i,max);
                }
                if (val < 0)
                    data.set(i, 0);
                i++;
            }

//            System.out.println("fill");

            //Following phase
            for (int id = 0; id < USER_PER_THREAD; id++) {

                userA = "user_"+Thread.currentThread().getName()+"_"+id;
                
                int nbFollow = data.get(random.nextInt(bound));
//                System.out.println(nbFollow);

                    for(int j = 0; j < nbFollow; j++){

                        n = random.nextInt(NB_USERS);
                        userB = users[n];

                        follow(userA, userB);
                    }
            }

        }

        public long compute(opType type) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, ClassNotFoundException, InstantiationException, InterruptedException {

            long startTime = 0L, endTime= 0L;
            int i, n = random.nextInt(USER_PER_THREAD * 2); // times 2 because we need to add new users that have not been already added
            String userA;
            String userB;

            switch (type){
                case ADD:
                    if (!mapFollowers.containsKey("user_"+Thread.currentThread().getName()+"_"+n)) {
                        startTime = System.nanoTime();
                        addUser("user_"+Thread.currentThread().getName()+"_"+n);
                        endTime = System.nanoTime();
                    }
                    break;
                case FOLLOW:
                    userA = "user_"+Thread.currentThread().getName()+"_"+n;
                    userB = null;
                    n = random.nextInt(mapFollowers.size());
                    i = 0;
                    for(Object obj : mapFollowers.keySet())
                    {
                        if (i == n){
                            userB = (String) obj;
                            break;
                        }
                        i++;
                    }

                    if (mapFollowers.containsKey(userA)){
                        startTime = System.nanoTime();
                        follow(userA, userB);
                        endTime = System.nanoTime();
                    }

                break;
                case UNFOLLOW:
                    userA = "user_"+Thread.currentThread().getName()+"_"+n;
                    userB = null;
                    n = random.nextInt(mapFollowers.size());
                    i = 0;
                    for(Object obj : mapFollowers.keySet())
                    {
                        if (i == n){
                            userB = (String) obj;
                            break;
                        }
                        i++;
                    }
                    if (mapFollowers.containsKey(userA)){
                        startTime = System.nanoTime();
                        unfollow(userA, userB);
                        endTime = System.nanoTime();
                    }
                break;
                case TWEET:
                    userA = "user_"+Thread.currentThread().getName()+"_"+n;
                    if (mapFollowers.containsKey(userA)){
                        startTime = System.nanoTime();
                        tweet(userA, "msg from " + userA);
                        endTime = System.nanoTime();
                    }
                break;
                case READ:
                    userA = "user_"+Thread.currentThread().getName()+"_"+n;
                    if (mapFollowers.containsKey(userA)){
                        startTime = System.nanoTime();
                        showTimeline(userA);
                        endTime = System.nanoTime();
                    }
                break;
                default:
                    throw new IllegalStateException("Unexpected value: " + type);
            }

            startTime = System.nanoTime();
            dummyFunction();
            endTime = System.nanoTime();

            return endTime - startTime;
        }

        public void addUser(String user) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
//            System.out.println("add");
//            nbFollower.put(user, (AbstractCounter) Factory.class.getDeclaredMethod(objectCounter).invoke(factory));
            mapFollowers.put(user, (AbstractSet) Factory.class.getDeclaredMethod(methodSet).invoke(factory));
            mapTimelines.put(user, new Timeline((AbstractQueue) Factory.class.getDeclaredMethod(methodQueue).invoke(factory),
                    (AbstractCounter) Factory.class.getDeclaredMethod(methodCounter).invoke(factory))
            );

        }

        // userA may not have been added yet

        public void follow(String userA, String userB){
//            System.out.println("follow");

            mapFollowers.get(userB).add(userA);
//          nbFollower.get(userB).increment();

        }

        public void unfollow(String userA, String userB){
//          System.out.println("unfollow");

            mapFollowers.get(userB).remove(userA);
//          nbFollower.get(userB).write(-1);

        }

        public void tweet(String user, String msg){
//            System.out.println("tweet");

            for (String follower : mapFollowers.get(user)) {
                mapTimelines.get(follower).add(msg);
            }

        }

        public void showTimeline(String user){
//            System.out.println("Show timeline");
            mapTimelines.get(user).read();
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
                if (_p)
                    System.out.println("Filling the database");

                latch.countDown();
                latch.await();

                if (_p){
                    System.out.println("Warming up");
                }
                TimeUnit.SECONDS.sleep(wTime);
                flag.set(false);
                if (_p){
                    System.out.println();
                    System.out.println("Computing");
                }
                TimeUnit.SECONDS.sleep(time);
                flag.set(true);
            } catch (InterruptedException e) {
                throw new Exception("Thread interrupted", e);
            }
            return null;
        }
    }
}
