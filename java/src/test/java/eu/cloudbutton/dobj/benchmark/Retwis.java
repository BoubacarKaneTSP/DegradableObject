package eu.cloudbutton.dobj.benchmark;

import eu.cloudbutton.dobj.incrementonly.BoxedLong;
import eu.cloudbutton.dobj.key.Key;
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
    private double _alphaInit = 1.7;

    @Option(name = "-alphaMin", usage = "min value tested for alpha (powerlaw settings)")
    private double _alphaMin = 1.7;

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
    private List<Float> allProportionMaxFollower;
    private List<Float> allProportionUserWithMaxFollower;
    private List<Float> allProportionUserWithoutFollower;

    private Database database;

    int NB_USERS;

    int nbSign = 5;

    int flag_append = 0;

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

        for (double i = _alphaInit ; i >= _alphaMin; i-=_alphaStep) {
            listAlpha.add(i);
        }

        NB_USERS = (int) _nbUserInit;

        if (_nbUserInit > _nbItems){
            System.out.println("Nb User must be lower or equal to number of hash");
            System.exit(1);
        }

        List<Integer> inPowerLawArrayFollowers = new DiscreteApproximate(1, 1.7).generate(100);
        List<Integer> outPowerLawArrayFollowers = new DiscreteApproximate(1, 1.6).generate(100);
        List<Integer> powerLawArrayUsers = new DiscreteApproximate(1, _alphaInit).generate(100);

        int index = 0;
        for (int val: inPowerLawArrayFollowers){
            if (val <= 0) {
                inPowerLawArrayFollowers.set(index, 1);
                outPowerLawArrayFollowers.set(index, 1);
                powerLawArrayUsers.set(index, 1);
            }

            index++;
        }

        for (int nbCurrThread = 1; nbCurrThread <= _nbThreads;) {

            if (_gcinfo)
                System.out.println("nbThread : "+nbCurrThread);

            PrintWriter printWriter = null;
            FileWriter fileWriter;
            long startTime, endTime, benchmarkAvgTime = 0;;
            allAvgQueueSizes = new ArrayList();
            allAvgFollower = new ArrayList();
            allProportionMaxFollower = new ArrayList();
            allProportionUserWithMaxFollower = new ArrayList();
            allProportionUserWithoutFollower = new ArrayList();

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
                    ExecutorService executor = Executors.newFixedThreadPool(nbCurrThread); // Additional count for the UserAdder
                    ExecutorService executorServiceCoordinator = Executors.newFixedThreadPool(1); // Coordinator

                    flagComputing = new AtomicBoolean(true);
                    flagWarmingUp = new AtomicBoolean(false);
                    database = new Database(typeMap, typeSet, typeQueue, typeCounter,
                            nbCurrThread,
                            (int) _nbUserInit,
                            _nbItems,
                            inPowerLawArrayFollowers,
                            outPowerLawArrayFollowers,
                            powerLawArrayUsers);

                    if (flag_append == 0 && nbCurrTest == 1){
                        flagWarmingUp.set(true);
                    }

                    CountDownLatch latch = new CountDownLatch(nbCurrThread+1); // Additional counts for the coordinator
                    CountDownLatch latchFillDatabase = new CountDownLatch(nbCurrThread);
                    CountDownLatch latchCompletionTime = new CountDownLatch(nbCurrThread+1);// Additional counts for the coordinator

                    for (int j = 0; j < nbCurrThread; j++) {
                        RetwisApp retwisApp = new RetwisApp(
                                latch,
                                latchFillDatabase,
                                latchCompletionTime
                        );
                        callables.add(retwisApp);
                    }

                    executorServiceCoordinator.submit(new Coordinator(latch, latchCompletionTime));
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
                                maxFollower = 0,
                                nbFollower,
                                userWithMaxFollower = 0,
                                userWithoutFollower = 0;

                        for(Key user: database.getUsersProbability().values()){
                            Set<Key> followers = database.getMapFollowers().get(user);
                            nbFollower = followers.size();
                            if (nbFollower > maxFollower) {
                                maxFollower = nbFollower;
                            }
                            nbFollowerTotal += nbFollower;
                        }
                        for(Key user: database.getUsersProbability().values()){
                            Set<Key> followers = database.getMapFollowers().get(user);
                            nbFollower = followers.size();

                            if (nbFollower>= maxFollower*0.8)
                                userWithMaxFollower++;
                            else if (nbFollower == 0)
                                userWithoutFollower++;
                        }

                        allAvgQueueSizes.add( (((float)queueSizes.intValue()/ NB_USERS)/nbCurrThread));
                        nbTweetFinal += queueSizes.longValue();
                        nbUserFinal += database.getMapTimelines().size();
                        allAvgFollower.add((float)nbFollowerTotal/NB_USERS);
                        allProportionMaxFollower.add((float) ((double)maxFollower/NB_USERS)*100);
                        allProportionUserWithMaxFollower.add((float) ((double)userWithMaxFollower/NB_USERS)*100);
                        allProportionUserWithoutFollower.add((float) ((double)userWithoutFollower/NB_USERS)*100);
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

                    String nameFile = "ALL_"+_tag+"_"+strAlpha+"_"+_nbUserInit+".txt";
                    if (flag_append == 0)
                        fileWriter = new FileWriter(nameFile, false);
                    else
                        fileWriter = new FileWriter(nameFile, true);

                    printWriter = new PrintWriter(fileWriter);
                    if (_completionTime)
                        printWriter.println(unit +" "+ completionTime/_nbTest);
                    else
                        printWriter.println(unit +" "+ (nbOpTotal / (double) timeTotalComputed) * 1_000_000_000);

                }

                if (_p){
                    for (int j = 0; j < nbSign; j++) System.out.print("-");

                    if (_completionTime) {
                        System.out.print(" ==> Completion time for " + _nbOps + " operations : ");
                        System.out.println(completionTime/1000000 + " milli secondes");

                    }
                    else {
                        System.out.print(" ==> Throughput (op/s) for all operations : ");
                        System.out.printf("%.3E%n",(nbOpTotal / (double) timeTotalComputed) * 1_000_000_000);
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

                        String nameFile = mapIntOptoStringOp.get(op)+"_"+_tag+"_"+strAlpha+"_"+_nbUserInit+".txt";
                        if (_s){
                            if (flag_append == 0)
                                fileWriter = new FileWriter( nameFile, false);
                            else
                                fileWriter = new FileWriter(nameFile, true);
                            printWriter = new PrintWriter(fileWriter);
                            printWriter.println(unit +" "+  (nbOp / (double) timeOp) * 1_000_000_000);
                        }

                        if (_p){
                            for (int j = 0; j < nbSign; j++) System.out.print("-");
                            System.out.print(" ==> Throughput (op/s) for "+mapIntOptoStringOp.get(op)+" : ");
                            System.out.println(String.format("%.3E", (nbOp / (double) timeOp) * 1_000_000_000));
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
                            sumProportionMaxFollower = 0,
                            sumProportionUserWithMaxFollower = 0,
                            sumProportionUserWithoutFollower = 0;

                    for (int i = 0; i < _nbTest; i++) {
                        sumAvgQueueSizes += allAvgQueueSizes.get(i);
                        sumAvgFollower += allAvgFollower.get(i);
                        sumProportionMaxFollower += allProportionMaxFollower.get(i);
                        sumProportionUserWithMaxFollower += allProportionUserWithMaxFollower.get(i);
                        sumProportionUserWithoutFollower += allProportionUserWithoutFollower.get(i);

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
                        System.out.println(" ==> % of the database that represent the max number of follower : " + sumProportionMaxFollower/_nbTest + "%");
                        System.out.println(" ==> % user with max follower (or 20% less) : " + sumProportionUserWithMaxFollower/_nbTest + "%");
                        System.out.println(" ==> % user without follower : " + sumProportionUserWithoutFollower/_nbTest + "%");
                        System.out.println(" ==> nb user at the end : " + nbUserFinal/_nbTest);
                        System.out.println();
                    }

                    if (_s){
                        FileWriter queueSizeFile, avgFollowerFile, proportionMaxFollowerFile, proportionUserWithMaxFollowerFile, proportionUserWithoutFollowerFile, nbUserFinalFile, nbTweetFinalFile;
                        PrintWriter queueSizePrint, avgFollowerPrint, proportionMaxFollowerPrint, proportionUserWithMaxFollowerPrint, nbUserWithoutFollowerPrint, nbUserFinalPrint, nbTweetFinalPrint;

                        boolean append = flag_append != 0;

                        String nameFile = _tag+"_"+strAlpha+"_"+_nbUserInit+".txt";
                        queueSizeFile = new FileWriter("avg_queue_size_" + nameFile, append);
                        avgFollowerFile = new FileWriter("avg_Follower_" + nameFile, append);
                        proportionMaxFollowerFile = new FileWriter("proportion_Max_Follower_" + nameFile, append);
                        proportionUserWithMaxFollowerFile = new FileWriter("proportion_User_With_Max_Follower_" + nameFile,append);
                        proportionUserWithoutFollowerFile = new FileWriter("proportion_User_Without_Follower_" + nameFile, append);
                        nbUserFinalFile = new FileWriter("nb_user_final_" + nameFile, append);
                        nbTweetFinalFile = new FileWriter("nb_tweet_final_" + nameFile, append);

                        queueSizePrint = new PrintWriter(queueSizeFile);
                        avgFollowerPrint = new PrintWriter(avgFollowerFile);
                        proportionMaxFollowerPrint = new PrintWriter(proportionMaxFollowerFile);
                        proportionUserWithMaxFollowerPrint = new PrintWriter(proportionUserWithMaxFollowerFile);
                        nbUserWithoutFollowerPrint = new PrintWriter(proportionUserWithoutFollowerFile);
                        nbUserFinalPrint = new PrintWriter(nbUserFinalFile);
                        nbTweetFinalPrint = new PrintWriter(nbTweetFinalFile);

                        queueSizePrint.println(unit + " " + sumAvgQueueSizes/_nbTest);
                        avgFollowerPrint.println(unit + " " + sumAvgFollower/_nbTest);
                        proportionMaxFollowerPrint.println(unit + " " + sumProportionMaxFollower/_nbTest);
                        proportionUserWithMaxFollowerPrint.println(unit + " " + sumProportionUserWithMaxFollower/_nbTest);
                        nbUserWithoutFollowerPrint.println(unit + " " + sumProportionUserWithoutFollower/_nbTest);
                        nbUserFinalPrint.println(unit + " " + nbUserFinal/_nbTest);
                        nbTweetFinalPrint.println(unit + " " + nbTweetFinal/_nbTest);

                        queueSizePrint.flush();
                        avgFollowerPrint.flush();
                        proportionMaxFollowerPrint.flush();
                        proportionUserWithMaxFollowerPrint.flush();
                        nbUserWithoutFollowerPrint.flush();
                        nbUserFinalPrint.flush();
                        nbTweetFinalPrint.flush();

                        queueSizeFile.close();
                        avgFollowerFile.close();
                        proportionMaxFollowerFile.close();
                        proportionUserWithMaxFollowerFile.close();
                        proportionUserWithoutFollowerFile.close();
                        nbUserFinalFile.close();
                        nbTweetFinalFile.close();
                    }
                }

                if(_p)
                    System.out.println();
                if (_s)
                    printWriter.close();
            }

            flag_append++;
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
        private final CountDownLatch latchFillCompletionTime;
        private Map<Key, Queue<Key>> usersFollow; // Local map that associate to each user, the list of user that it follows
        private Long usersProbabilityRange;
        private Long localUsersProbabilityRange;
        private int nbRepeat = 1000;
        private final String msg = "new msg";
        int nbLocalUsers;
        int nbAttempt;
        Key userB, userA;
	    long startTime, endTime;
        Map<Integer, BoxedLong> nbLocalOperations;
        Map<Integer, BoxedLong> timeLocalOperations;

        public RetwisApp(CountDownLatch latch,CountDownLatch latchFillDatabase, CountDownLatch latchFillCompletionTime) {
            this.random = ThreadLocal.withInitial(() -> new Random(94));
            this.ratiosArray = Arrays.stream(distribution).mapToInt(Integer::parseInt).toArray();
            this.latch = latch;
            this.latchFillDatabase = latchFillDatabase;
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

                database.fill(latchFillDatabase, usersFollow);

                latch.countDown();
                latch.await();

                usersProbabilityRange = database.getUsersProbabilityRange();
                localUsersProbabilityRange = database.getLocalUsersProbabilityRange().get();
                nbLocalUsers = database.getLocalUsersProbability().get().size();

                while (flagWarmingUp.get()) { // warm up
                    type = chooseOperation();
                    compute(type, nbLocalOperations, timeLocalOperations);
                }

                long startTimeBenchmark, endTimeBenchmark;

                startTimeBenchmark = System.nanoTime();
                if (_completionTime){
                    int nbOperationToDo = (int) (_nbOps/ database.getNbThread());
                    for (int i = 0; i < nbOperationToDo; i++) {
                        type = chooseOperation();
                        compute(type, nbLocalOperations, timeLocalOperations);
                    }
                }else{
                    while (flagComputing.get()){

                        type = chooseOperation();

                        if (_multipleOperation){
                            for (int j = 0; j < nbRepeat; j++) {
                                compute(type, nbLocalOperations, timeLocalOperations);
                            }
                        }else{
                            compute(type, nbLocalOperations, timeLocalOperations);
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

            } catch (InterruptedException | InvocationTargetException | IllegalAccessException | ClassNotFoundException | InstantiationException | NoSuchMethodException | OutOfMemoryError e) {
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

        public void compute(int type, Map<Integer, BoxedLong> nbOps, Map<Integer, BoxedLong> timeOps) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, ClassNotFoundException, InstantiationException, InterruptedException {

            startTime = 0L;
            endTime= 0L;
            nbAttempt = -1;

            int nbAttemptMax = (int) (Math.log(0.01)/Math.log((nbLocalUsers-1) / (double) nbLocalUsers));

            int typeComputed = type;
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

                long val = random.get().nextLong()%localUsersProbabilityRange;
                userA = database.getLocalUsersProbability().get().ceilingEntry(val).getValue();
                Queue<Key> listFollow = usersFollow.get(userA);
                switch (typeComputed){
                    case ADD:
                        startTime = System.nanoTime();
                        database.addUser(database.generateUser());
                        endTime = System.nanoTime();
                        break;
                    case FOLLOW:

                        val = random.get().nextLong()%usersProbabilityRange; // We choose a user to follow according to a probability
                        userB = database.getUsersProbability().ceilingEntry(val).getValue();

                        if (!listFollow.contains(userB)){ // Perform follow only if userB is not already followed
                            startTime = System.nanoTime();
                            database.followUser(userA, userB);
                            endTime = System.nanoTime();

                            listFollow.add(userB);
                        }else
                            continue restartOperation;

                        break;
                    case UNFOLLOW:
                        userB = listFollow.poll();
                        if (userB != null){ // Perform unfollow only if userA already follow someone
                            startTime = System.nanoTime();
                            database.unfollowUser(userA, userB);
                            endTime = System.nanoTime();
                        }else
                            continue restartOperation;
                        break;
                    case TWEET:
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

                if (!flagWarmingUp.get()) {
                    nbOps.get(typeComputed).val += 1;
                    timeOps.get(typeComputed).val+= endTime - startTime;
                }

                break;
            }
        }

        private void flushTimelines(){
            for (Key user : usersFollow.keySet()) {
                database.getMapTimelines().get(user).clear();
            }
        }
    }

    public class Coordinator implements Callable<Void> {

        private final CountDownLatch latch;
        private final CountDownLatch latchCompletionTime;

        public Coordinator(CountDownLatch latch, CountDownLatch latchCompletionTime) {
            this.latch = latch;
            this.latchCompletionTime = latchCompletionTime;
        }

        @Override
        public Void call() throws Exception {
            try {

                if (_p)
                    System.out.println(" ==> Filling the database with "+ NB_USERS +" users" );

                if (flagWarmingUp.get()){

                    latch.countDown();
                    latch.await();

                    if (_p){
                        System.out.println(" ==> Warming up for " + _wTime + " seconds");
                    }

                    TimeUnit.SECONDS.sleep(_wTime);

                    flagWarmingUp.set(false);
                }
                else{
                    latch.countDown();
                    latch.await();
                }

                if (_gcinfo)
                    System.out.println("Start benchmark");
                if (! _completionTime) {
                    if (_p) {
                        System.out.println(" ==> Computing the throughput for "+ _time +" seconds");
                    }
                    TimeUnit.SECONDS.sleep(_time);
                    flagComputing.set(false);

                }else{

                    long startTime, endTime;

                    if (_p)
                        System.out.println(" ==> Computing the completion time for " + _nbOps + " operations");

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
    }
}
