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

    private Map<String, AtomicInteger> nbOperations;
    private Map<String, AtomicLong> timeOperations;

    private String[] listOperations = new String[]{"add", "follow", "unfollow", "tweet", "read"};

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


        Factory factory = new Factory();
        String objectSet = "create" + typeSet;
        String objectQueue = "create" + typeQueue;
        String objectCounter = "create" + typeCounter;

        /*
        String objectSet = "create" + typeSet;
        String objectQueue = "create" + typeQueue;
        String objectCounter = "create" + typeCounter;*/

        List<Double> listAlpha = new ArrayList<>();

        for (double i = _alphaInit ; i >= _alphaMin; i-=_alphaStep) {
            listAlpha.add(i);
        }

        for (int i = 1; i <= nbThreads;) {

            PrintWriter printWriter = null;
            FileWriter fileWriter;

            if (_p){
                System.out.println();
                for (int j = 0; j < 2*nbSign; j++) System.out.print("*");
                System.out.print( " Results for ["+i+"] threads ");
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

                for (int a = 1; a <= nbTest; a++) {
                    java.util.List<Callable<Void>> callables = new ArrayList<>();
                    ExecutorService executor = Executors.newFixedThreadPool(i);

                    if (a == 1) {
                        nbOperations = new ConcurrentHashMap<>();
                        timeOperations = new ConcurrentHashMap<>();

                        for (String op : listOperations) {
                            nbOperations.put(op, new AtomicInteger());
                            timeOperations.put(op, new AtomicLong(0));
                        }
                    }

                    CountDownLatch latch = new CountDownLatch(i+1); // Additional count for the coordinator
                    CountDownLatch latchFillDatabase = new CountDownLatch(i);

                    for (int j = 0; j < i; j++) {
                        RetwisApp retwisApp = new RetwisApp(
                                objectSet,
                                objectQueue,
                                objectCounter,
                                Arrays.stream(ratios).mapToInt(Integer::parseInt).toArray(),
                                alpha,
                                latch,
                                latchFillDatabase,
                                factory,
                                i);
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

                double nbTotalOperations = 0;
                long timeTotalOperations = 0L;

                for (String op : listOperations){
                    nbTotalOperations += nbOperations.get(op).get();
                    timeTotalOperations += timeOperations.get(op).get();
                }

                if (_s){

                    if (i == 1)
                        fileWriter = new FileWriter("retwis_all_operations.txt", false);
                    else
                        fileWriter = new FileWriter("retwis_all_operations.txt", true);

                    printWriter = new PrintWriter(fileWriter);
                    printWriter.println(i +" "+ nbTotalOperations / ((double)timeTotalOperations/1_000_000_000));
                }

                if (_p){
                    for (int j = 0; j < nbSign; j++) System.out.print("-");
                    System.out.print(" Time per operations for all type of operations ");
                    for (int j = 0; j < nbSign; j++) System.out.print("-");
                    System.out.println();
                    System.out.println(" - "+ nbTotalOperations / ((double)timeTotalOperations/1_000_000_000));

                }

                if (_s)
                    printWriter.flush();

                for (String op: listOperations){
                    if (_s){
                        if (i == 1)
                            fileWriter = new FileWriter("retwis_"+op+"_operations.txt", false);
                        else
                            fileWriter = new FileWriter("retwis_"+op+"_operations.txt", true);
                        printWriter = new PrintWriter(fileWriter);
                        printWriter.println(i +" "+  (double)nbOperations.get(op).get()/((double)timeOperations.get(op).get()/1_000_000_000));
                    }

                    if (_p){
                        for (int j = 0; j < nbSign; j++) System.out.print("-");
                        System.out.print(" Time per operations for "+op+" operations ");
                        for (int j = 0; j < nbSign; j++) System.out.print("-");
                        System.out.println();
                        System.out.println(" - "+ (double)nbOperations.get(op).get() / ((double)timeOperations.get(op).get()/1_000_000_000));
                    }

                    if (_s)
                        printWriter.flush();
                }
                if(_p)
                    System.out.println();
                if (_s)
                    printWriter.close();

            }



            i *= 2;
            if (i > nbThreads && i != 2 * nbThreads)
                i = nbThreads;
        }
        System.exit(0);
    }

    public class RetwisApp implements Callable<Void>{

        private  final int NB_USERS = 500000;
        protected int ITEM_PER_THREAD;
        protected final ThreadLocalRandom random;
        private final String objectSet;
        private final String objectQueue;
        private final String objectCounter;
        private final int[] ratios;
        private final double alpha;
        private final CountDownLatch latch;
        private final CountDownLatch latchFillDatabase;
        private final Factory factory;
        private Map<String, AbstractSet<String>> follower;
        private Map<String, AbstractCounter> nbFollower;
        private Map<String, Timeline> timeline;

        public RetwisApp(String objectSet, String objectQueue, String objectCounter, int[] ratios, double alpha, CountDownLatch latch,CountDownLatch latchFillDatabase, Factory factory, int nbThread) {
            this.random = ThreadLocalRandom.current();
            this.objectSet = objectSet;
            this.objectQueue = objectQueue;
            this.objectCounter = objectCounter;
            this.ratios = ratios;
            this.alpha = alpha;
            this.latch = latch;
            this.latchFillDatabase = latchFillDatabase;
            this.factory = factory;
            this.ITEM_PER_THREAD = NB_USERS / nbThread; // the loop start with j=0
            this.follower = new ConcurrentHashMap<>();
            this.nbFollower = new ConcurrentHashMap<>();
            this.timeline = new ConcurrentHashMap<>();
        }

        @Override
        public Void call(){

            char type;
            int val;

            Map<String, Integer> nbLocalOperations = new HashMap<>();
            Map<String, Long> timeLocalOperations = new HashMap<>();

            for (String op: listOperations){
                nbLocalOperations.put(op, 0);
                timeLocalOperations.put(op, 0L);
            }

            try{

                fill_database();

                latch.countDown();

                latch.await();

                //warm up

                while (flag.get()){
                    val = random.nextInt(100);

                    if(val < ratios[0]){ // add
                        type = 'a';
                    }else if (val >= ratios[0] && val < ratios[0]+ratios[1]){ //follow or unfollow
                        if (val%2 == 0){ //follow
                            type = 'f';
                        }else{ //unfollow
                            type = 'u';
                        }
                    }else if (val >= ratios[0]+ratios[1] && val < ratios[0]+ratios[1]+ratios[2]){ //tweet
                        type = 't';
                    }else{ //read
                        type = 'r';
                    }

                    compute(type);
                }

                while (!flag.get()){

                    val = random.nextInt(100);

                    if(val < ratios[0]){ // add
                        type = 'a';
                    }else if (val >= ratios[0] && val < ratios[0]+ratios[1]){ //follow or unfollow
                        if (val%2 == 0){ //follow
                            type = 'f';
                        }else{ //unfollow
                            type = 'u';
                        }
                    }else if (val >= ratios[0]+ratios[1] && val < ratios[0]+ratios[1]+ratios[2]){ //tweet
                        type = 't';
                    }else{ //read
                        type = 'r';
                    }

                    long elapsedTime = compute(type);

                    switch (type){
                        case 'a':
                            nbLocalOperations.compute("add", (key, value) -> value + 1);
                            timeLocalOperations.compute("add", (key, value) -> value + elapsedTime);
                            break;
                        case 'f':
                            nbLocalOperations.compute("follow", (key, value) -> value + 1);
                            timeLocalOperations.compute("follow", (key, value) -> value + elapsedTime);
                            break;
                        case 'u':
                            nbLocalOperations.compute("unfollow", (key, value) -> value + 1);
                            timeLocalOperations.compute("unfollow", (key, value) -> value + elapsedTime);
                            break;
                        case 't':
                            nbLocalOperations.compute("tweet", (key, value) -> value + 1);
                            timeLocalOperations.compute("tweet", (key, value) -> value + elapsedTime);
                            break;
                        case 'r':
                            nbLocalOperations.compute("read", (key, value) -> value + 1);
                            timeLocalOperations.compute("read", (key, value) -> value + elapsedTime);
                            break;
                    }
                }

                for (String op: listOperations){
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
            for (int i = 0; i < ITEM_PER_THREAD; i++) {
                addUser("user_"+Thread.currentThread().getName()+"_"+i);
            }

            latchFillDatabase.countDown();

            latchFillDatabase.await();

            int nbUsers = follower.keySet().size();
            String[] users = follower.keySet().toArray(new String[nbUsers]);

            int bound = 1000;
            
            List<Integer> data = new DiscreteApproximate(1, alpha).generate(bound);

            int i = 0, max = 100000/(175000000/nbUsers); //10⁵ is ~ the number of follow max on twitter and 175000000 is the number of user on twitter (stats from the article)

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
            for (i = 0; i < ITEM_PER_THREAD; i++) {

                userA = "user_"+Thread.currentThread().getName()+"_"+i;
                
                int nbFollow = data.get(random.nextInt(bound));
//                System.out.println(nbFollow);

                    for(int j = 0; j < nbFollow; j++){

                        n = random.nextInt(nbUsers);
                        userB = users[n];

                        follow(userA, userB);
                    }
            }

        }

        public long compute(char type) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, ClassNotFoundException, InstantiationException {

            long startTime = 0L, endTime= 0L;
            int i, n = random.nextInt(ITEM_PER_THREAD*2);
            String userA;
            String userB;

            switch (type){
                case 'a':
                    if (!follower.containsKey("user_"+Thread.currentThread().getName()+"_"+n)) {
                        startTime = System.nanoTime();
                        addUser("user_"+Thread.currentThread().getName()+"_"+n);
                        endTime = System.nanoTime();
                    }
                    break;
                case 'f':
                    userA = "user_"+Thread.currentThread().getName()+"_"+n;
                    userB = null;
                    n = random.nextInt(follower.size());
                    i = 0;
                    for(Object obj : follower.keySet())
                    {
                        if (i == n){
                            userB = (String) obj;
                            break;
                        }
                        i++;
                    }

                    if (follower.containsKey(userA)){
                        startTime = System.nanoTime();
                        follow(userA, userB);
                        endTime = System.nanoTime();
                    }

                break;
                case 'u':
                    userA = "user_"+Thread.currentThread().getName()+"_"+n;
                    userB = null;
                    n = random.nextInt(follower.size());
                    i = 0;
                    for(Object obj : follower.keySet())
                    {
                        if (i == n){
                            userB = (String) obj;
                            break;
                        }
                        i++;
                    }
                    if (follower.containsKey(userA)){
                        startTime = System.nanoTime();
                        unfollow(userA, userB);
                        endTime = System.nanoTime();
                    }
                break;
                case 't':
                    userA = "user_"+Thread.currentThread().getName()+"_"+n;
                    if (follower.containsKey(userA)){
                        startTime = System.nanoTime();
                        tweet(userA, "msg from " + userA);
                        endTime = System.nanoTime();
                    }
                break;
                case 'r':
                    userA = "user_"+Thread.currentThread().getName()+"_"+n;
                    if (follower.containsKey(userA)){
                        startTime = System.nanoTime();
                        showTimeline(userA);
                        endTime = System.nanoTime();
                    }
                break;
                default:
                    throw new IllegalStateException("Unexpected value: " + type);
            }
            return endTime - startTime;
        }

        public void addUser(String user) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException, InstantiationException {
//            System.out.println("add");

            AbstractQueue queue;
            AbstractCounter counter;
            AbstractSet set;
            Class classQueue, classSet, classCounter;

            try{
                classQueue = Class.forName("eu.cloudbutton.dobj.types."+typeQueue);
            }catch (ClassNotFoundException e){
                classQueue = Class.forName("java.util.concurrent."+typeQueue);
            }

            try{
                classSet = Class.forName("eu.cloudbutton.dobj.types."+typeSet);
            }catch (ClassNotFoundException e){
                classSet = Class.forName("java.util.concurrent."+typeSet);
            }

            try{
                classCounter = Class.forName("eu.cloudbutton.dobj.types."+typeCounter);
            }catch (ClassNotFoundException e){
                classCounter = Class.forName("java.util.concurrent."+typeCounter);
            }


            queue = (AbstractQueue) classQueue.getConstructor().newInstance();
            set = (AbstractSet) classSet.getConstructor().newInstance();
            counter = (AbstractCounter) classCounter.getConstructor().newInstance();

            follower.put(user, set);
//            nbFollower.put(user, (AbstractCounter) Factory.class.getDeclaredMethod(objectCounter).invoke(factory));
            timeline.put(user, new Timeline(queue, counter));

        }

        // userA may not have been added yet

        public void follow(String userA, String userB){
//            System.out.println("follow");

            follower.get(userB).add(userA);
//          nbFollower.get(userB).increment();

        }

        public void unfollow(String userA, String userB){
//          System.out.println("unfollow");

            follower.get(userB).remove(userA);
//          nbFollower.get(userB).write(-1);

        }

        public void tweet(String user, String msg){
//            System.out.println("tweet");

            for (String u : follower.get(user)) {
                timeline.get(u).add(msg);
            }

        }

        public void showTimeline(String user){
//            System.out.println("Show timeline");
            timeline.get(user).read();
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
