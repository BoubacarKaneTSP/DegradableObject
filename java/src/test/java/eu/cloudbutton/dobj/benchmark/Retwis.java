package eu.cloudbutton.dobj.benchmark;

import eu.cloudbutton.dobj.Timeline;
import eu.cloudbutton.dobj.asymmetric.CounterMISD;
import eu.cloudbutton.dobj.incrementonly.BoxedLong;
import eu.cloudbutton.dobj.key.Key;
import nl.peterbloem.powerlaws.DiscreteApproximate;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.ExplicitBooleanOptionHandler;
import org.kohsuke.args4j.spi.StringArrayOptionHandler;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.kohsuke.args4j.OptionHandlerFilter.ALL;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

public class Retwis {

    enum opType{
        ADD,
        FOLLOW,
        UNFOLLOW,
        TWEET,
        READ
    }

    private static final int ADD = 0, FOLLOW = 1, UNFOLLOW = 2, TWEET = 3, READ = 4;
    private static final int NBOPDIFF = 5;
    private static final Map<Integer, String> mapIntOptoStringOp = new HashMap<>(){{
        put(ADD, "ADD");
        put(FOLLOW, "FOLLOW");
        put(UNFOLLOW, "UNFOLLOW");
        put(TWEET, "TWEET");
        put(READ, "READ");
    }};

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

    @Option(name = "-nbUserInit", usage = "Number of user initially added")
    private long _nbUserInit = 1_000_000;

    @Option(name = "-time", usage = "test time (seconds)")
    private long _time = 20;

    @Option(name = "-wTime", usage = "warming time (seconds)")
    private long _wTime = 5;

    @Option(name = "-alphaInit", usage = "first value tested for alpha (powerlaw settings)")
    private double _alphaInit = 0.9;

    @Option(name = "-alphaMin", usage = "min value tested for alpha (powerlaw settings)")
    private double _alphaMin = 0.9;

    @Option(name = "-alphaStep", usage = "step between two value tested for alpha (powerlaw settings)")
    private double _alphaStep = 0.2;

    @Option(name="-tag", required = false, usage = "tag of result's file")
    private String _tag;

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

    @Option(name = "-breakdown", handler = ExplicitBooleanOptionHandler.class, usage = "Print the details results for all operations")
    public boolean _breakdown = false;

    @Option(name = "-gcinfo", handler = ExplicitBooleanOptionHandler.class, usage = "Compute gc info")
    public boolean _gcinfo = false;

    @Option(name = "-collisionKey", handler = ExplicitBooleanOptionHandler.class, usage = "Testing map with collision on key")
    public boolean _collisionKey = false;

    @Option(name = "-heapDump", handler = ExplicitBooleanOptionHandler.class, usage = "Computing heap dump")
    public boolean _heapDump = false;

    @Option(name = "-nbItems", usage = "Number of items max per thread")
    private int _nbItems = Integer.MAX_VALUE;

    private AtomicBoolean flagComputing,flagWarmingUp;

    private List<AtomicLong> nbOperations;
    private List<AtomicLong> timeOperations;
    private LongAdder timeBenchmark;
    private LongAdder queueSizes;
    private Long nbUserFinal;
    private Long nbTweetFinal;
    private List<Float> allAvgQueueSizes;
    private List<Float> allAvgFollower;
    private List<Float> allAvgFollowing;
    private List<Float> allProportionMaxFollower;
    private List<Float> allProportionMaxFollowing;
    private List<Float> allProportionUserWithMaxFollower;
    private List<Float> allProportionUserWithMaxFollowing;
    private List<Float> allProportionUserWithoutFollower;
    private List<Float> allProportionUserWithoutFollowing;

    private Database database;

    int NB_USERS;

    int nbSign = 5;

    int flag_append = 1;

    private long completionTime;

    public static void main(String[] args) throws InterruptedException, IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, NoSuchFieldException {
/*        Queue queue1 = new LinkedList();
        KeyGenerator keyGenerator = new RetwisKeyGenerator(1000000, 1000000, 1.39);
        Key retwisKey = keyGenerator.nextKey();
        Key key = new ThreadLocalKey(1, 1000, 10000000);
        System.out.println("node size :");
        System.out.println(ClassLayout.parseClass(queue1.getClass().getDeclaredField("last").getDeclaringClass()).toPrintable());
        System.out.println("ThreadLocalKey size :");
        System.out.println(ClassLayout.parseClass(key.getClass()).toPrintable());
        System.out.println("RetwisKey size :");
        System.out.println(ClassLayout.parseClass(retwisKey.getClass()).toPrintable());
        System.out.println("Max memory : " + Runtime.getRuntime().maxMemory());*/
        new Retwis().doMain(args);
    }

    public void doMain(String[] args) throws InterruptedException, IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, OutOfMemoryError {
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
            System.err.println("  Example: java eu.cloudbutton.dobj.benchmark.Retwis" + parser.printExample(ALL));

            return;
        }

        if (_p)
            System.out.println(" ==> Launching test from App.java, a clone of Retwis...");

        List<Double> listAlpha = new ArrayList<>();

//        for (double i = _alphaInit ; i >= _alphaMin; i-=_alphaStep) {
//            listAlpha.add(i);
//        }
        listAlpha.add(_alphaInit);

        NB_USERS = (int) _nbUserInit;

        if (_nbUserInit > _nbItems){
            System.out.println("Nb User must be lower or equal to number of hash");
            System.exit(1);
        }

        List<Integer> powerLawArray = new DiscreteApproximate(1, 1.7).generate(NB_USERS);

        int index = 0;
        for (int val: powerLawArray){
            if (val <= 0) {
                val = 1;
                powerLawArray.set(index, val);
            }
            index++;
        }


        for (int nbCurrThread = _nbThreads; nbCurrThread <= _nbThreads;) {

            if (_gcinfo) {
                System.out.println("nbThread : "+nbCurrThread);
            }

            flag_append = nbCurrThread == 1 ? 0 : 1;

            PrintWriter printWriter = null;
            FileWriter fileWriter;
            String nameFile;

            long startTime, endTime, benchmarkAvgTime = 0;;
            allAvgQueueSizes = new ArrayList();
            allAvgFollower = new ArrayList();
            allAvgFollowing = new ArrayList();
            allProportionMaxFollower = new ArrayList();
            allProportionMaxFollowing = new ArrayList();
            allProportionUserWithMaxFollower = new ArrayList();
            allProportionUserWithMaxFollowing = new ArrayList();
            allProportionUserWithoutFollower = new ArrayList();
            allProportionUserWithoutFollowing = new ArrayList();

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

                nbOperations = new CopyOnWriteArrayList<>();
                timeOperations = new CopyOnWriteArrayList<>();
                queueSizes = new LongAdder();
                nbUserFinal = 0L;
                nbTweetFinal = 0L;
                timeBenchmark = new LongAdder();
                completionTime = 0;

                for (int op: mapIntOptoStringOp.keySet()) {
                    nbOperations.add(op, new AtomicLong());
                    timeOperations.add(op, new AtomicLong());
                }

                for (int nbCurrTest = 1; nbCurrTest <= _nbTest; nbCurrTest++) {
                    List<Callable<Void>> callables = new ArrayList<>();
                    ExecutorService executor = Executors.newFixedThreadPool(nbCurrThread);
                    ExecutorService executorServiceCoordinator = Executors.newFixedThreadPool(1); // Coordinator

                    flagComputing = new AtomicBoolean(true);
                    flagWarmingUp = new AtomicBoolean(false);
                    database = new Database(typeMap, typeSet, typeQueue, typeCounter,
                            nbCurrThread,
                            (int) _nbUserInit,
                            _nbItems,
                            powerLawArray);

                    if (nbCurrTest == 1){
                        flagWarmingUp.set(true);
                    }

                    CountDownLatch latch = new CountDownLatch(nbCurrThread+1); // Additional counts for the coordinator
                    CountDownLatch latchFillDatabase = new CountDownLatch(nbCurrThread);
                    CountDownLatch latchHistogramDatabase = new CountDownLatch(nbCurrThread);
                    CountDownLatch latchCompletionTime = new CountDownLatch(nbCurrThread+1);// Additional counts for the coordinator

                    for (int j = 0; j < nbCurrThread; j++) {
                        RetwisApp retwisApp = new RetwisApp(
                                latch,
                                latchFillDatabase,
                                latchHistogramDatabase,
                                latchCompletionTime
                        );
                        callables.add(retwisApp);
                    }

                    executorServiceCoordinator.submit(new Coordinator(latch, latchCompletionTime, latchHistogramDatabase));
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


                    benchmarkAvgTime += endTime - startTime;

                    if(_p)
                        System.out.println(" ==> End of test num : " + nbCurrTest);

                    TimeUnit.SECONDS.sleep(1);

                    if (_breakdown){

                        int nbFollowerTotal = 0,
                                nbFollowingTotal = 0,
                                maxFollower = 0,
                                maxFollowing = 0,
                                nbFollower,
                                nbFollowing,
                                userWithMaxFollower = 0,
                                userWithMaxFollowing = 0,
                                userWithoutFollower = 0,
                                userWithoutFollowing = 0;

                        for(Key user: database.getUsersFollowProbability().values()){
                            Set<Key> followers = database.getMapFollowers().get(user);
                            Set<Key> followings = database.getMapFollowing().get(user);

                            nbFollower = followers.size();
                            nbFollowing = followings.size();

                            if (nbFollower > maxFollower) {
                                maxFollower = nbFollower;
                            }
                            nbFollowerTotal += nbFollower;

                            if (nbFollowing > maxFollowing) {
                                maxFollowing = nbFollowing;
                            }
                            nbFollowingTotal += nbFollowing;
                        }

                        for(Key user: database.getUsersFollowProbability().values()){
                            Set<Key> followers = database.getMapFollowers().get(user);
                            Set<Key> followings = database.getMapFollowing().get(user);

                            nbFollower = followers.size();
                            nbFollowing = followings.size();

                            if (nbFollower>= maxFollower*0.8)
                                userWithMaxFollower++;
                            else if (nbFollower == 0)
                                userWithoutFollower++;

                            if (nbFollowing>= maxFollowing*0.8)
                                userWithMaxFollowing++;
                            else if (nbFollowing == 0)
                                userWithoutFollowing++;
                        }

                        allAvgQueueSizes.add( (((float)queueSizes.intValue()/ NB_USERS)/nbCurrThread));
                        nbTweetFinal += queueSizes.longValue();
                        nbUserFinal += database.getMapTimelines().size();
                        allAvgFollower.add((float)nbFollowerTotal/NB_USERS);
                        allAvgFollowing.add((float)nbFollowingTotal/NB_USERS);
                        allProportionMaxFollower.add((float) ((double)maxFollower/NB_USERS)*100);
                        allProportionMaxFollowing.add((float) ((double)maxFollowing/NB_USERS)*100);
                        allProportionUserWithMaxFollower.add((float) ((double)userWithMaxFollower/NB_USERS)*100);
                        allProportionUserWithMaxFollowing.add((float) ((double)userWithMaxFollowing/NB_USERS)*100);
                        allProportionUserWithoutFollower.add((float) ((double)userWithoutFollower/NB_USERS)*100);
                        allProportionUserWithoutFollowing.add((float) ((double)userWithoutFollowing/NB_USERS)*100);
                    }
                    executor.shutdown();
                }

                if(_p)
                    System.out.println();

                if (_gcinfo || _p)
                    System.out.println("benchmarkAvgTime : " + (benchmarkAvgTime / 1000000)/_nbTest );

                long nbOpTotal = 0, timeTotalComputed = 0;

                int unit = nbCurrThread;

                for (int op: mapIntOptoStringOp.keySet()) {
                    nbOpTotal += nbOperations.get(op).get();
                    timeTotalComputed += timeOperations.get(op).get();
                }

                if (_p)
                    System.out.println(" ==> Results :");

                long nbOp, timeOp;
                String strAlpha = Double.toString(alpha).replace(".","");
                if (strAlpha.length() >= 3)
                    strAlpha = strAlpha.substring(0,3);

                long timeBenchmarkAvg = ((timeBenchmark.longValue() / 1_000_000) / nbCurrThread) / _nbTest;

                if (_s){

//                    nameFile = "ALL_"+_tag+"_"+strAlpha+"_"+_nbUserInit+".txt";
                    nameFile = "ALL_"+_tag+"_"+_nbUserInit+".txt";

//                    boolean append = flag_append != 0;

                    fileWriter = new FileWriter(nameFile, true);

                    printWriter = new PrintWriter(fileWriter);
                    if (_completionTime)
                        printWriter.println(unit +" "+ completionTime/_nbTest);
                    else
                        printWriter.println(unit +" "+ ((nbOpTotal / (double) timeTotalComputed) * nbCurrThread) * 1_000_000_000);

                }

                if (_p){
                    for (int j = 0; j < nbSign; j++) System.out.print("-");

                    if (_completionTime) {
                        System.out.print(" ==> Completion time for " + _nbOps + " operations : ");
                        System.out.println(completionTime/1000000 + " milli secondes");

                    }
                    else {
                        System.out.print(" ==> Throughput (op/s) for all operations : ");
                        System.out.printf("%.3E%n",((nbOpTotal / (double) timeTotalComputed) * nbCurrThread) * 1_000_000_000);
                        System.out.println(" ==> - temps d'execution  : "+ (timeTotalComputed/nbCurrThread)/1_000_000 + "ms");
                    }

                    System.out.println();
                }

                if (_s)
                    printWriter.flush();

                if (! _completionTime){
                    for (int op: mapIntOptoStringOp.keySet()){

                        nbOp = nbOperations.get(op).get();
                        timeOp = timeOperations.get(op).get();

//                    timeOperations.get(op).set( timeOperations.get(op).get()/nbCurrThread );  // Compute the avg time to get the global throughput

//                        nameFile = mapIntOptoStringOp.get(op)+"_"+_tag+"_"+strAlpha+"_"+_nbUserInit+".txt";
                        nameFile = mapIntOptoStringOp.get(op)+"_"+_tag+"_"+_nbUserInit+".txt";
                        if (_s){

//                            boolean append = flag_append != 0;

                            fileWriter = new FileWriter( nameFile, true);

                            printWriter = new PrintWriter(fileWriter);
                            printWriter.println(unit +" "+  ((nbOp / (double) timeOp) * nbCurrThread) * 1_000_000_000);
                        }

                        if (_p){
                            for (int j = 0; j < nbSign; j++) System.out.print("-");
                            System.out.print(" ==> Throughput (op/s) for "+mapIntOptoStringOp.get(op)+" : ");
                            System.out.println(String.format("%.3E", ((nbOp / (double) timeOp) * nbCurrThread) * 1_000_000_000));
                            System.out.println();
                        }

                        if (_s)
                            printWriter.flush();
                    }
                }

                if (_gcinfo || _p){
                    System.out.println("Avg benchmark time (without warmup) : " + timeBenchmarkAvg + "ms");
                }

                if (_breakdown){

                    float sumAvgQueueSizes = 0,
                            sumAvgFollower = 0,
                            sumAvgFollowing = 0,
                            sumProportionMaxFollower = 0,
                            sumProportionMaxFollowing = 0,
                            sumProportionUserWithMaxFollower = 0,
                            sumProportionUserWithMaxFollowing = 0,
                            sumProportionUserWithoutFollower = 0,
                            sumProportionUserWithoutFollowing = 0;

                    for (int i = 0; i < _nbTest; i++) {
                        sumAvgQueueSizes += allAvgQueueSizes.get(i);
                        sumAvgFollower += allAvgFollower.get(i);
                        sumAvgFollowing += allAvgFollowing.get(i);
                        sumProportionMaxFollower += allProportionMaxFollower.get(i);
                        sumProportionMaxFollowing += allProportionMaxFollowing.get(i);
                        sumProportionUserWithMaxFollower += allProportionUserWithMaxFollower.get(i);
                        sumProportionUserWithMaxFollowing += allProportionUserWithMaxFollowing.get(i);
                        sumProportionUserWithoutFollower += allProportionUserWithoutFollower.get(i);
                        sumProportionUserWithoutFollowing += allProportionUserWithoutFollowing.get(i);

                    }
                    if (_p){
                        System.out.println("Stats for each op over (" + _nbTest + ") tests :");
                        for (int op: mapIntOptoStringOp.keySet()) {
                            int nbSpace = 10 - mapIntOptoStringOp.get(op).length();
                            System.out.print("==> - " + mapIntOptoStringOp.get(op));
                            for (int i = 0; i < nbSpace; i++) System.out.print(" ");
                            System.out.println(": Nb op : " + nbOperations.get(op).get()
                                    + ", proportion : " + (int) ((nbOperations.get(op).get() / (double) nbOpTotal) * 100) + "%"
                                    + ", temps d'exécution : " + (timeOperations.get(op).get()/nbCurrThread) / 1_000 + " micro seconds");
                        }

                        System.out.println(" ==> avg sum time op : " + ((timeTotalComputed/1_000_000)/nbCurrThread)/_nbTest + " ms");
                        System.out.println(" ==> nb original users : " + NB_USERS);
                        System.out.println(" ==> nb Tweet at the end : " + nbTweetFinal/_nbTest);
                        System.out.println(" ==> avg queue size : " + sumAvgQueueSizes/_nbTest);
                        System.out.println(" ==> avg follower : " + sumAvgFollower/_nbTest);
                        System.out.println(" ==> avg following : " + sumAvgFollowing/_nbTest);
                        System.out.println(" ==> % of the database that represent the max number of follower : " + sumProportionMaxFollower/_nbTest + "%");
                        System.out.println(" ==> % of the database that represent the max number of following : " + sumProportionMaxFollowing/_nbTest + "%");
                        System.out.println(" ==> % user with max follower (or 20% less) : " + sumProportionUserWithMaxFollower/_nbTest + "%");
                        System.out.println(" ==> % user with max following (or 20% less) : " + sumProportionUserWithMaxFollowing/_nbTest + "%");
                        System.out.println(" ==> % user without follower : " + sumProportionUserWithoutFollower/_nbTest + "%");
                        System.out.println(" ==> % user without following : " + sumProportionUserWithoutFollowing/_nbTest + "%");
                        System.out.println(" ==> nb user at the end : " + nbUserFinal/_nbTest);
                        System.out.println();
                    }

                    if (_s){
                        FileWriter queueSizeFile,
                                avgFollowerFile,
                                avgFollowingFile,
                                proportionMaxFollowerFile,
                                proportionMaxFollowingFile,
                                proportionUserWithMaxFollowerFile,
                                proportionUserWithMaxFollowingFile,
                                proportionUserWithoutFollowerFile,
                                proportionUserWithoutFollowingFile,
                                nbUserFinalFile,
                                nbTweetFinalFile;

                        PrintWriter queueSizePrint,
                                avgFollowerPrint,
                                avgFollowingPrint,
                                proportionMaxFollowerPrint,
                                proportionMaxFollowingPrint,
                                proportionUserWithMaxFollowerPrint,
                                proportionUserWithMaxFollowingPrint,
                                nbUserWithoutFollowerPrint,
                                nbUserWithoutFollowingPrint,
                                nbUserFinalPrint,
                                nbTweetFinalPrint;

                        boolean append = true;
//                        boolean append = flag_append != 0;

//                        nameFile = _tag+"_"+strAlpha+"_"+_nbUserInit+".txt";
                        nameFile = _tag+"_"+_nbUserInit+".txt";

                        queueSizeFile = new FileWriter("avg_queue_size_" + nameFile, append);
                        avgFollowerFile = new FileWriter("avg_Follower_" + nameFile, append);
                        avgFollowingFile = new FileWriter("avg_Following_" + nameFile, append);
                        proportionMaxFollowerFile = new FileWriter("proportion_Max_Follower_" + nameFile, append);
                        proportionMaxFollowingFile = new FileWriter("proportion_Max_Following_" + nameFile, append);
                        proportionUserWithMaxFollowerFile = new FileWriter("proportion_User_With_Max_Follower_" + nameFile,append);
                        proportionUserWithMaxFollowingFile = new FileWriter("proportion_User_With_Max_Following_" + nameFile,append);
                        proportionUserWithoutFollowerFile = new FileWriter("proportion_User_Without_Follower_" + nameFile, append);
                        proportionUserWithoutFollowingFile = new FileWriter("proportion_User_Without_Following_" + nameFile, append);
                        nbUserFinalFile = new FileWriter("nb_user_final_" + nameFile, append);
                        nbTweetFinalFile = new FileWriter("nb_tweet_final_" + nameFile, append);

                        queueSizePrint = new PrintWriter(queueSizeFile);
                        avgFollowerPrint = new PrintWriter(avgFollowerFile);
                        avgFollowingPrint = new PrintWriter(avgFollowingFile);
                        proportionMaxFollowerPrint = new PrintWriter(proportionMaxFollowerFile);
                        proportionMaxFollowingPrint = new PrintWriter(proportionMaxFollowingFile);
                        proportionUserWithMaxFollowerPrint = new PrintWriter(proportionUserWithMaxFollowerFile);
                        proportionUserWithMaxFollowingPrint = new PrintWriter(proportionUserWithMaxFollowingFile);
                        nbUserWithoutFollowerPrint = new PrintWriter(proportionUserWithoutFollowerFile);
                        nbUserWithoutFollowingPrint = new PrintWriter(proportionUserWithoutFollowingFile);
                        nbUserFinalPrint = new PrintWriter(nbUserFinalFile);
                        nbTweetFinalPrint = new PrintWriter(nbTweetFinalFile);

                        queueSizePrint.println(unit + " " + sumAvgQueueSizes/_nbTest);
                        avgFollowerPrint.println(unit + " " + sumAvgFollower/_nbTest);
                        avgFollowingPrint.println(unit + " " + sumAvgFollowing/_nbTest);
                        proportionMaxFollowerPrint.println(unit + " " + sumProportionMaxFollower/_nbTest);
                        proportionMaxFollowingPrint.println(unit + " " + sumProportionMaxFollowing/_nbTest);
                        proportionUserWithMaxFollowerPrint.println(unit + " " + sumProportionUserWithMaxFollower/_nbTest);
                        proportionUserWithMaxFollowingPrint.println(unit + " " + sumProportionUserWithMaxFollowing/_nbTest);
                        nbUserWithoutFollowerPrint.println(unit + " " + sumProportionUserWithoutFollower/_nbTest);
                        nbUserWithoutFollowingPrint.println(unit + " " + sumProportionUserWithoutFollowing/_nbTest);
                        nbUserFinalPrint.println(unit + " " + nbUserFinal/_nbTest);
                        nbTweetFinalPrint.println(unit + " " + nbTweetFinal/_nbTest);

                        queueSizePrint.flush();
                        avgFollowerPrint.flush();
                        avgFollowingPrint.flush();
                        proportionMaxFollowerPrint.flush();
                        proportionMaxFollowingPrint.flush();
                        proportionUserWithMaxFollowerPrint.flush();
                        proportionUserWithMaxFollowingPrint.flush();
                        nbUserWithoutFollowerPrint.flush();
                        nbUserWithoutFollowingPrint.flush();
                        nbUserFinalPrint.flush();
                        nbTweetFinalPrint.flush();

                        queueSizeFile.close();
                        avgFollowerFile.close();
                        avgFollowingFile.close();
                        proportionMaxFollowerFile.close();
                        proportionMaxFollowingFile.close();
                        proportionUserWithMaxFollowerFile.close();
                        proportionUserWithMaxFollowingFile.close();
                        proportionUserWithoutFollowerFile.close();
                        proportionUserWithoutFollowingFile.close();
                        nbUserFinalFile.close();
                        nbTweetFinalFile.close();
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

        private final ThreadLocal<Random> random;
        private final int[] ratiosArray;
        private final CountDownLatch latch;
        private final CountDownLatch latchFillDatabase;
        private final CountDownLatch latchHistogramDatabase;
        private final CountDownLatch latchFillCompletionTime;
        private Map<Key, Queue<Key>> usersFollow; // Local map that associate to each user, the list of user that it follows
        private Long localUsersProbabilityRange;
        private Long usersFollowProbabilityRange;
        private int nbRepeat = 1000;
        private final String msg = "new msg";
        int nbLocalUsers;
        int nbAttempt;
        Key userB, userA, dummyUser;
        Set<Key> dummySet;
        Timeline<String> dummyTimeline;
        long startTime, endTime;
        Map<Integer, BoxedLong> nbLocalOperations;
        Map<Integer, BoxedLong> timeLocalOperations;

        public RetwisApp(CountDownLatch latch,CountDownLatch latchFillDatabase, CountDownLatch latchHistogramDatabase, CountDownLatch latchFillCompletionTime) {
            this.random = ThreadLocal.withInitial(() -> new Random(94));
            this.ratiosArray = Arrays.stream(distribution).mapToInt(Integer::parseInt).toArray();
            this.latch = latch;
            this.latchFillDatabase = latchFillDatabase;
            this.latchHistogramDatabase = latchHistogramDatabase;
            this.latchFillCompletionTime = latchFillCompletionTime;
            this.usersFollow = new HashMap<>();
        }

        @Override
        public Void call(){

            try{
                int type;

                nbLocalOperations = new HashMap<>();
                timeLocalOperations = new HashMap<>();

                for (int op: mapIntOptoStringOp.keySet()){
                    nbLocalOperations.put(op, new BoxedLong());
                    timeLocalOperations.put(op, new BoxedLong());
                }

                database.fill(latchFillDatabase, latchHistogramDatabase, usersFollow);

                latch.countDown();
                latch.await();

                localUsersProbabilityRange = database.getLocalUsersUsageProbabilityRange().get();
                usersFollowProbabilityRange = database.getUsersFollowProbabilityRange();
                nbLocalUsers = database.getLocalUsersUsageProbability().get().size();

                dummyUser = database.generateUser();
                dummySet = new HashSet<>();
                dummyTimeline = new Timeline<>(new LinkedList<>());

                int num = 0;
                boolean cleanTimeline = false;
                while (flagWarmingUp.get()) { // warm up
                    type = chooseOperation();
                    compute(type, nbLocalOperations, timeLocalOperations, num++,cleanTimeline);

                    cleanTimeline = num % (2 * _nbUserInit) == 0;
                }


                resetAllTimeline();

                long startTimeBenchmark, endTimeBenchmark;

                startTimeBenchmark = System.nanoTime();
                if (_completionTime){
                    int nbOperationToDo = (int) (_nbOps/ database.getNbThread());
                    for (int i = 0; i < nbOperationToDo; i++) {
                        type = chooseOperation();
                        compute(type, nbLocalOperations, timeLocalOperations, i, cleanTimeline);
                        cleanTimeline = i % (2 * _nbUserInit) == 0;

                    }
                }else{

                    num=0;

                    while (flagComputing.get()){

                        type = chooseOperation();

                        if (_multipleOperation){
                            for (int j = 0; j < nbRepeat; j++) {
                                compute(type, nbLocalOperations, timeLocalOperations, num++,cleanTimeline);
                                cleanTimeline = num % (2 * _nbUserInit) == 0;

                            }
                        }else{
                            compute(type, nbLocalOperations, timeLocalOperations, num++,cleanTimeline);
                            cleanTimeline = num % (2 * _nbUserInit) == 0;
                        }
                    }

                }

                endTimeBenchmark = System.nanoTime();

                if (_completionTime){
                    latchFillCompletionTime.countDown();
                    latchFillCompletionTime.await();
                }

                timeBenchmark.add(endTimeBenchmark - startTimeBenchmark);

                for (Key user : usersFollow.keySet()){
                    queueSizes.add(database.getMapTimelines().get(user).getTimeline().size());
                }

                for (int op: mapIntOptoStringOp.keySet()){
                    nbOperations.get(op).addAndGet(nbLocalOperations.get(op).val);
                    timeOperations.get(op).addAndGet(timeLocalOperations.get(op).val);
                }

            } catch (InterruptedException | InvocationTargetException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException | InstantiationException e) {
                e.printStackTrace();
            }
            return null;
        }

        public void dummyFunction() throws InterruptedException {
            TimeUnit.MICROSECONDS.sleep(1000);
        }

        public int chooseOperation(){
            int type;

            int val = random.get().nextInt(100);
            if(val < ratiosArray[0]){ // add
                type = ADD;
            }else if (val >= ratiosArray[0] && val < ratiosArray[0]+ ratiosArray[1]){ //follow or unfollow
                if (val%2 == 0){ //follow
                    type = FOLLOW;
                }else{ //unfollow
                    type = UNFOLLOW;
                }
            }else if (val >= ratiosArray[0]+ ratiosArray[1] && val < ratiosArray[0]+ ratiosArray[1]+ ratiosArray[2]){ //tweet
                type = TWEET;
            }else{ //read
                type = READ;
            }

            return type;
        }

        public void compute(int type, Map<Integer, BoxedLong> nbOps, Map<Integer, BoxedLong> timeOps, int num, boolean cleanTimeline) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, ClassNotFoundException, InstantiationException, InterruptedException {

            startTime = 0L;
            endTime= 0L;
            nbAttempt = -1;

            int nbAttemptMax = (int) (Math.log(0.01)/Math.log((nbLocalUsers-1) / (double) nbLocalUsers));

            int typeComputed = type;

            if (cleanTimeline){

                typeComputed = READ;

                for (int i = 0; i < nbLocalUsers; i++) {
                    userA = database.getListLocalUser().get(database.getThreadID().get()).get(i);

                    startTime = System.nanoTime();
                    database.showTimeline(userA);
                    endTime = System.nanoTime();

                    if (!flagWarmingUp.get()) {
                        nbOps.get(typeComputed).val += 1;
                        timeOps.get(typeComputed).val += endTime - startTime;
                    }
                }

            }
            else{
                /*To avoid infinite loop if :
                 * - When doing follow, all user handle by thread i already follow all users in usersProbability.
                 * - When doing unfollow, all user handle by thread i do not follow anyone.
                 *
                 * We use an int nbAttempt to change the operation after an amount of fail
                 * When probability of not doing an operation on userA is less than 1%.
                 * */
                restartOperation : for (;;){
                    if (nbLocalUsers == 0)
                        break ;
                    nbAttempt ++;
                    if (nbAttempt > nbAttemptMax)
                        typeComputed = chooseOperation();

                    int val = random.get().nextInt(nbLocalUsers);
                    userA = database.getListLocalUser().get(database.getThreadID().get()).get(val);

                   /* if (val < nbLocalUsers)
                        userA = database.getListLocalUser().get(database.getThreadID().get()).get((int) (val%(nbLocalUsers*0.1)));
                    else {
//                userA = database.getLocalUsersUsageProbability().get().ceilingEntry(val).getValue();
                        System.out.println("Error choosing user");
                        System.exit(0);
                        userA = database.getListLocalUser().get(database.getThreadID().get()).get(num % nbLocalUsers);
                    }
*/
                    Queue<Key> listFollow = usersFollow.get(userA);

                    switch (typeComputed){
                        case ADD:
                            startTime = System.nanoTime();
                            database.addUser(dummyUser,dummySet, dummyTimeline);
                            endTime = System.nanoTime();
                            database.removeUser(dummyUser);
                            break;
                        case FOLLOW:

                            int val2 = random.get().nextInt(nbLocalUsers);
                            userB = database.getListLocalUser().get(database.getThreadID().get()).get(val2);
//                            long val2 = random.get().nextLong()%usersFollowProbabilityRange; // We choose a user to follow according to a probability
//                            userB = database.getUsersFollowProbability().ceilingEntry(val2).getValue();

                            if (!listFollow.contains(userB) && userB != null){ // Perform follow only if userB is not already followed
                                startTime = System.nanoTime();
                                database.followUser(userA, userB);
                                endTime = System.nanoTime();
//                        listFollow.add(userB);
                                database.unfollowUser(userA,userB);

                            }else
                                continue restartOperation;

                            break;
                        case UNFOLLOW:

                            if (listFollow.size() == 0)
                                continue restartOperation;

//                        val = random.get().nextInt(listFollow.size());
//                        int v = 0;
//                        for (Key user : listFollow){
//                            if (v==val) {
//                                userB = user;
//                                break;
//                            }
//                            v++;
//                        }

                            userB = listFollow.poll();
                            if (userB != null){ // Perform unfollow only if userA already follow someone
                                startTime = System.nanoTime();
                                database.unfollowUser(userA, userB);
                                endTime = System.nanoTime();

                                database.followUser(userA, userB);
                                listFollow.add(userB);
                            }else
                                continue restartOperation;
                            break;
                        case TWEET:
                            startTime = System.nanoTime();
                            database.tweet(userA, msg);
                            endTime = System.nanoTime();
//                        database.realtweet(userA, msg);
                            break;
                        case READ:
                            startTime = System.nanoTime();
                            database.showTimeline(userA);
                            endTime = System.nanoTime();

                            break;
                        default:
                            throw new IllegalStateException("Unexpected value: " + type);
                    }

                    if (!flagWarmingUp.get()) {
                        nbOps.get(typeComputed).val += 1;
                        timeOps.get(typeComputed).val+= endTime - startTime;
                    }

                    break;
                }
            }

        }

        public void resetAllTimeline(){
            for (Key usr: database.getLocalUsersUsageProbability().get().values()){
                database.getMapTimelines().get(usr).clear();
            }
        }
    }

    public class Coordinator implements Callable<Void> {

        private final CountDownLatch latchFillDatabase;
        private final CountDownLatch latchCompletionTime;
        private final CountDownLatch latchHistogram;

        public Coordinator(CountDownLatch latchFillDatabase, CountDownLatch latchCompletionTime, CountDownLatch latchHistogram) {
            this.latchFillDatabase = latchFillDatabase;
            this.latchCompletionTime = latchCompletionTime;
            this.latchHistogram = latchHistogram;
        }

        @Override
        public Void call() throws Exception {
            try {

                if (_p)
                    System.out.println(" ==> Filling the database with "+ NB_USERS +" users" );

                System.out.println(flagWarmingUp.get());
                if (flagWarmingUp.get()){

                    latchHistogram.await();

//                    saveDistributionHistogram("Pre_Benchmark");

//                    performHeapDump(_tag, "Pre", (int) _nbUserInit);

                    latchFillDatabase.countDown();
                    latchFillDatabase.await();

                    if (_p){
                        System.out.println(" ==> Warming up for " + _wTime + " seconds");
                    }

                    TimeUnit.SECONDS.sleep(_wTime);

                    flagWarmingUp.set(false);
                }
                else{
                    latchFillDatabase.countDown();
                    latchFillDatabase.await();
                }

                if (_gcinfo)
                    System.out.println("Start benchmark");
                if (! _completionTime) {
                    if (_p) {
                        System.out.println(" ==> Computing the throughput for "+ _time +" seconds");
                    }
//                    startMonitoring();
                    TimeUnit.SECONDS.sleep(_time);
                    flagComputing.set(false);
//                    stopMonitoring();
//                    TimeUnit.SECONDS.sleep(2);
//                    performHeapDump(_tag, "Post", (int) _nbUserInit);

//                    saveTimelineHistogram();
//                    TimeUnit.SECONDS.sleep(5);
//                    saveDistributionHistogram("Post_Benchmark");
                }else{

                    long startTime, endTime;

                    if (_p) {
                        System.out.println(" ==> Computing the completion time for " + _nbOps + " operations");
                    }

                    startTime = System.nanoTime();

                    latchCompletionTime.countDown();
                    latchCompletionTime.await();

                    endTime = System.nanoTime();

                    completionTime += endTime - startTime;
                }


                if (_gcinfo)
                    System.out.println("End benchmark");

            } catch (InterruptedException e) {
                throw new Exception("Thread interrupted", e);
            }
            return null;
        }

        private void saveDistributionHistogram(String tag) throws IOException {
            if (_p){
                System.out.println("Saving "+ tag +" distribution histogram");
            }

            int range = 5;
            int max = 100;
            String distributionHistogramFollower, distributionHistogramFollowing;

            distributionHistogramFollower = database.computeHistogram(range, max,"Follower");
            distributionHistogramFollowing = database.computeHistogram(range, max,"Following");

//            Map<Integer,Integer> mapHistogramFollower, mapHistogramFollowing;
//
//            mapHistogramFollower = database.computeHistogram(range, max,"Follower");
//            mapHistogramFollowing = database.computeHistogram(range, max,"Following");

            PrintWriter printWriter;
            FileWriter fileWriter;

            fileWriter = new FileWriter("Follower_Distribution_"+ _tag + "_" + tag + "_" + _nbUserInit + "_Users_" + _nbThreads + "_Threads.txt", false);
            printWriter = new PrintWriter(fileWriter);

            printWriter.println(distributionHistogramFollower);

//            for (Integer k : mapHistogramFollower.keySet())
//                printWriter.println(k + " " + mapHistogramFollower.get(k));

            printWriter.flush();
            fileWriter.close();

            fileWriter = new FileWriter("Following_Distribution_" + _tag + "_" + tag + "_" + _nbUserInit + "_Users_" + _nbThreads + "_Threads.txt", false);
            printWriter = new PrintWriter(fileWriter);

            printWriter.println(distributionHistogramFollowing);
//            for (Integer k : mapHistogramFollowing.keySet())
//                printWriter.println(k + " " + mapHistogramFollowing.get(k));

            printWriter.flush();
            fileWriter.close();
        }

        private void saveTimelineHistogram() throws IOException {
            if (_p){
                System.out.println("saving timeline histogram");
            }

            PrintWriter printWriter;
            FileWriter fileWriter;

            fileWriter = new FileWriter("Timeline_Distribution_"+ _tag +"_"+ _nbUserInit + "_Users_" + _nbThreads + "_Threads.txt", false);
            printWriter = new PrintWriter(fileWriter);

            String txt = "";

            for (Timeline<String> timeline : database.getMapTimelines().values()){
                txt += timeline.getTimeline().size() + " ";
            }

            printWriter.print(txt);
            printWriter.flush();
            fileWriter.close();
        }
    }

    private static void performHeapDump(String tag, String when, int nbUser) {
        System.out.println("Performing heapDump");
        String jcmdCommand = "jcmd";
        String processId = getProcessId();

        try {
            Process process = Runtime.getRuntime().exec(jcmdCommand);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            while ((line = reader.readLine()) != null) {
                // Recherche de la ligne contenant le processus Java souhaité
                if (line.contains(processId)) {
                    String[] tokens = line.trim().split("\\s+");
                    String pid = tokens[0];
                    String heapDumpCommand = "jcmd " + pid + " GC.heap_dump " + "heapdump_"+ when +"_benchmark_" + tag +"_" + nbUser +".hprof";

                    // Exécution de la commande jcmd pour effectuer le heap dump
                    Process heapDumpProcess = Runtime.getRuntime().exec(heapDumpCommand);

                    // Attente de la fin de l'exécution de la commande
                    int exitCode = heapDumpProcess.waitFor();

                    if (exitCode == 0) {
                        System.out.println("Heap dump effectué avec succès !");
                    } else {
                        System.out.println("Erreur lors de l'exécution de la commande jcmd.");
                    }

                    break;
                }
            }

            reader.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static String getProcessId() {
        String processName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
        return processName.split("@")[0];
    }

    public void startMonitoring(){
        if (_p){
            System.out.println("===> Starting monitoring with BTrace");
        }
    }
    public void stopMonitoring(){
        if (_p){
            System.out.println("===> Ending monitoring.");
        }
    }
}
