package eu.cloudbutton.dobj;

import eu.cloudbutton.dobj.types.*;
import nl.peterbloem.powerlaws.Discrete;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Localizable;
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
import java.util.Set;
import java.util.concurrent.*;

import static org.kohsuke.args4j.OptionHandlerFilter.ALL;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class App {

    @Option(name="-set", required = true, usage = "type of Set")
    private String typeSet;

    @Option(name="-list", required = true, usage = "type of List")
    private String typeList;

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

    @Option(name = "-s", handler = ExplicitBooleanOptionHandler.class, usage = "Save the result")
    private boolean _s = false;

    @Option(name = "-p", handler = ExplicitBooleanOptionHandler.class, usage = "Print the result")
    private boolean _p = false;

    private AtomicBoolean flag;
    private Map<String, AbstractSet<String>> follower;
    private Map<String, AbstractCounter> nbFollower;
    private Map<String, Timeline> timeline;

    private Map<String, DegradableCounter> nbOperations;
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
            System.err.println("  Example: java eu.cloudbutton.dobj.App" + parser.printExample(ALL));

            return;
        }

        if (_p)
            System.out.println("Launching test from App.java, a clone of Retwis...");


        Factory factory = new Factory();
        String objectSet = "create" + typeSet;
        String objectList = "create" + typeList;
        String objectCounter = "create" + typeCounter;

        for (int i = 1; i <= nbThreads;) {
            for (int a = 1; a <= nbTest; a++) {
                java.util.List<Callable<Void>> callables = new ArrayList<>();
                ExecutorService executor = Executors.newFixedThreadPool(i);

                follower = new ConcurrentHashMap<>();
                nbFollower = new ConcurrentHashMap<>();
                timeline = new ConcurrentHashMap<>();

                if (a == 1) {
                    nbOperations = new ConcurrentHashMap<>();
                    timeOperations = new ConcurrentHashMap<>();

                    for (String op : listOperations) {
                        nbOperations.put(op, new DegradableCounter());
                        timeOperations.put(op, new AtomicLong(0));
                    }
                }

                CountDownLatch latch = new CountDownLatch(i+1); // Additional count for the coordinator

                for (int j = 0; j < i; j++) {
                    RetwisApp retwisApp = new RetwisApp(
                            objectSet,
                            objectList,
                            objectCounter,
                            Arrays.stream(ratios).mapToInt(Integer::parseInt).toArray(),
                            latch, factory);
                    callables.add(retwisApp);
                }

                ExecutorService executorService = Executors.newFixedThreadPool(1);
                flag = new AtomicBoolean();
                flag.set(true);
                executorService.submit(new Coordinator(latch));

                List<Future<Void>> futures;
                futures = executor.invokeAll(callables);

                try{
                    for (Future<Void> future : futures) {
                        future.get();
                    }
                } catch (CancellationException | ExecutionException e) {
                    //ignore
                    System.out.println(e);
                }
                TimeUnit.SECONDS.sleep(5);
                executor.shutdown();
            }

            double nbTotalOperations = 0;
            long timeTotalOperations = 0L;

            for (String op : listOperations){
                nbTotalOperations += nbOperations.get(op).read();
                timeTotalOperations += timeOperations.get(op).get();
            }

            PrintWriter printWriter = null;
            FileWriter fileWriter;

            if (_p){
                System.out.println();
                for (int j = 0; j < 2*nbSign; j++) System.out.print("*");
                System.out.print( " Results for ["+i+"] threads ");
                for (int j = 0; j < 2*nbSign; j++) System.out.print("*");
                System.out.println();
            }

            if (_s){

                if (i == 1)
                    fileWriter = new FileWriter("retwis_all_operations.txt", false);
                else
                    fileWriter = new FileWriter("retwis_all_operations.txt", true);

                printWriter = new PrintWriter(fileWriter);
                printWriter.println(i +" "+ (double)timeTotalOperations/1_000_000_000/nbTotalOperations);
            }

            if (_p){
                for (int j = 0; j < nbSign; j++) System.out.print("-");
                System.out.print(" Time per operations for all type of operations ");
                for (int j = 0; j < nbSign; j++) System.out.print("-");
                System.out.println();
                System.out.println(" - "+ (double)timeTotalOperations/1_000_000_000/nbTotalOperations);

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
                    printWriter.println(i +" "+ (double)timeOperations.get(op).get()/1_000_000_000/(double)nbOperations.get(op).read());
                }

                if (_p){
                    for (int j = 0; j < nbSign; j++) System.out.print("-");
                    System.out.print(" Time per operations for "+op+" operations ");
                    for (int j = 0; j < nbSign; j++) System.out.print("-");
                    System.out.println();
                    System.out.println(" - "+(double)timeOperations.get(op).get()/1_000_000_000/(double)nbOperations.get(op).read());
                }

                if (_s)
                    printWriter.flush();
            }
            if(_p)
                System.out.println();
            if (_s)
                printWriter.close();

            i *= 2;
            if (i > nbThreads && i != 2 * nbThreads)
                i = nbThreads;
        }
        System.exit(0);
    }

    public class RetwisApp implements Callable<Void>{

        protected static final int ITEM_PER_THREAD = 100;
        protected final ThreadLocalRandom random;
        private final String objectSet;
        private final String objectList;
        private final String objectCounter;
        private final int[] ratios;
        private final CountDownLatch latch;
        private final Factory factory;

        public RetwisApp(String objectSet, String objectList, String objectCounter, int[] ratios, CountDownLatch latch, Factory factory) {
            this.random = ThreadLocalRandom.current();
            this.objectSet = objectSet;
            this.objectList = objectList;
            this.objectCounter = objectCounter;
            this.ratios = ratios;
            this.latch = latch;
            this.factory = factory;
        }

        @Override
        public Void call(){

            char type;
            int val;
            long startTime, endTime;

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

                    startTime = System.nanoTime();
                    compute(type);
                    endTime = System.nanoTime();

                    long finalEndTime = endTime;
                    long finalStartTime = startTime;

                    switch (type){
                        case 'a':
                            nbLocalOperations.compute("add", (key, value) -> value + 1);
                            timeLocalOperations.compute("add", (key, value) -> value + finalEndTime - finalStartTime);
                            break;
                        case 'f':
                            nbLocalOperations.compute("follow", (key, value) -> value + 1);
                            timeLocalOperations.compute("follow", (key, value) -> value + finalEndTime - finalStartTime);
                            break;
                        case 'u':
                            nbLocalOperations.compute("unfollow", (key, value) -> value + 1);
                            timeLocalOperations.compute("unfollow", (key, value) -> value + finalEndTime - finalStartTime);
                            break;
                        case 't':
                            nbLocalOperations.compute("tweet", (key, value) -> value + 1);
                            timeLocalOperations.compute("tweet", (key, value) -> value + finalEndTime - finalStartTime);
                            break;
                        case 'r':
                            nbLocalOperations.compute("read", (key, value) -> value + 1);
                            timeLocalOperations.compute("read", (key, value) -> value + finalEndTime - finalStartTime);
                            break;
                    }
                }

                for (String op: listOperations){
                    nbOperations.get(op).increment(nbLocalOperations.get(op));
                    timeOperations.get(op).addAndGet(timeLocalOperations.get(op));
                }

            } catch (InterruptedException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
            }
            return null;
        }

        public void dummyFunction() throws InterruptedException {
            TimeUnit.MICROSECONDS.sleep(10000);
        }

        public void fill_database() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {

            int n;
            String userA;
            String userB;

            //adding users
            for (int i = 0; i < ITEM_PER_THREAD/2; i++) {
                addUser("user_"+Thread.currentThread().getName()+"_"+i);
            }

            List<Integer> data = new Discrete(1, 1.35).generate(200);

            //Following phase
            for (int i = 0; i < ITEM_PER_THREAD/2; i++) {

                userA = "user_"+Thread.currentThread().getName()+"_"+i;
                int nbFollow = Math.min(data.get(random.nextInt(200)), follower.keySet().size());
//                System.out.println("nb follow du process '"+ userA +"' : " + nbFollow);

                for(int j = 0; j < nbFollow; j++){

                    n = random.nextInt(follower.keySet().size());

                    userB = (String) follower.keySet().toArray()[n];

                    follow(userA, userB);
                }
            }
        }

        public void compute(char type) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {

            int i, n = random.nextInt(ITEM_PER_THREAD);
            String userA;
            String userB;

            switch (type){
                case 'a':
                    addUser("user_"+Thread.currentThread().getName()+"_"+n);
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
//                    userB = (String) follower.keySet().toArray()[n];
//                    userB = (String) follower.keySet().toArray()[n];
                    follow(userA, userB);
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
                    unfollow(userA, userB);
                break;
                case 't':
                    userA = "user_"+Thread.currentThread().getName()+"_"+n;
                    tweet(userA, "msg from " + userA);
                break;
                case 'r':
                    userA = "user_"+Thread.currentThread().getName()+"_"+n;
                    showTimeline(userA);
                break;
            }
        }

        public void addUser(String user) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
//            System.out.println("add");

            if(!follower.containsKey(user)) {
                follower.put(user, (AbstractSet) Factory.class.getDeclaredMethod(objectSet).invoke(factory));
                nbFollower.put(user, (AbstractCounter) Factory.class.getDeclaredMethod(objectCounter).invoke(factory));
                timeline.put(user, new Timeline((AbstractQueue) Factory.class.getDeclaredMethod(objectList).invoke(factory),
                                (AbstractCounter) Factory.class.getDeclaredMethod(objectCounter).invoke(factory))
                                                );
            }
        }

        // userA may not have been added yet

        public void follow(String userA, String userB){
//            System.out.println("follow");

//            if(follower.containsKey(userA)) {
//                follower.get(userB).add(userA);
////                nbFollower.get(userB).increment();
//            }
        }

        public void unfollow(String userA, String userB){
//            System.out.println("unfollow");

            if(follower.containsKey(userA)) {
                follower.get(userB).remove(userA);
//                nbFollower.get(userB).write(-1);
            }
        }

        public void tweet(String user, String msg){
//            System.out.println("tweet");

            if(follower.containsKey(user)) {
                for (String u : follower.get(user)) {
                    timeline.get(u).add(msg);
                }
            }
        }

        public void showTimeline(String user){
//            System.out.println("Show timeline");
            if(follower.containsKey(user)) {
                timeline.get(user).read();
//                System.out.println(s);
            }
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
                latch.countDown();
                latch.await();

                if (_p){
                    System.out.println("warming up");
                }
                TimeUnit.SECONDS.sleep(wTime);
                flag.set(false);
                if (_p){
                    System.out.println();
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
