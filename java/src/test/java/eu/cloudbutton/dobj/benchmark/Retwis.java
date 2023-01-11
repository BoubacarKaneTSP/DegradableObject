package eu.cloudbutton.dobj.benchmark;

import eu.cloudbutton.dobj.incrementonly.BoxedLong;
import eu.cloudbutton.dobj.key.Key;
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
    private long _nbOps = 1_000_00;

    @Option(name = "-time", usage = "test time (seconds)")
    private long _time = 20;

    @Option(name = "-wTime", usage = "warming time (seconds)")
    private long _wTime = 5;

    @Option(name = "-alphaInit", usage = "first value tested for alpha (powerlaw settings)")
    private double _alphaInit = 1.39;

    @Option(name = "-alphaMin", usage = "min value tested for alpha (powerlaw settings)")
    private double _alphaMin = 1.39;

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
    private List<Integer> allAvgQueueSizes;
    private List<Integer> allAvgFollower;
    private List<Integer> allNbMaxFollower;
    private List<Integer> allNbUserWithMaxFollower;
    private List<Integer> allNbUserWithoutFollower;

    private Database database;

    int NB_USERS;

    int nbSign = 5;

    int flag_append = 0;

    public static void main(String[] args) throws InterruptedException, IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, NoSuchFieldException {
//        Queue queue1 = new QueueMASP();
//        System.out.println(ClassLayout.parseClass(queue1.getClass().getDeclaredField("head").getDeclaringClass()).toPrintable());
        new Retwis().doMain(args);
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
            System.err.println("  Example: java eu.cloudbutton.dobj.benchmark.Retwis" + parser.printExample(ALL));

            return;
        }

        if (_p)
            System.out.println(" ==> Launching test from App.java, a clone of Retwis...");

        List<Double> listAlpha = new ArrayList<>();

        for (double i = _alphaInit ; i >= _alphaMin; i-=_alphaStep) {
            listAlpha.add(i);
        }

        for (int nbCurrThread = 1; nbCurrThread <= _nbThreads;) {

            if (_gcinfo)
                System.out.println("nbThread : "+nbCurrThread);

            PrintWriter printWriter = null;
            FileWriter fileWriter;
            long startTime, endTime, timeTotal = 0L, benchmarkAvgTime = 0;;
            allAvgQueueSizes = new ArrayList();
            allAvgFollower = new ArrayList();
            allNbMaxFollower = new ArrayList();
            allNbUserWithMaxFollower = new ArrayList();
            allNbUserWithoutFollower = new ArrayList();
            NB_USERS = (int) _nbOps;
//            NB_USERS = nbCurrThread;

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
                timeBenchmark = new LongAdder();

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
                    database = new Database(typeMap, typeSet, typeQueue, typeCounter, alpha, nbCurrThread, _collisionKey, _nbItems);

                    if (flag_append == 0 && nbCurrTest == 1){
                        flagWarmingUp.set(true);
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

                    if (flagWarmingUp.get()) {
                        benchmarkAvgTime -= _wTime * 1_000_000_000;
                    }

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
                    timeTotal = endTime - startTime;

                    if (nbCurrThread == 1 && nbCurrTest == 1)

                    if (flagWarmingUp.get()) {
                        timeTotal -= _wTime * 1_000_000_000;
                    }
                    if(_p)
                        System.out.println(" ==> End of test num : " + nbCurrTest);

                    TimeUnit.SECONDS.sleep(1);

                    if (_breakdown){

                        int nbFollowerTotal = 0,
                                maxFollower = 0,
                                nbFollower,
                                userWithMaxFollower = 0,
                                userWithoutFollower = 0;

                        for(Key user: database.getOriginalUsers()){
                            Set followers = database.getMapFollowers().get(user);
                            nbFollower = followers.size();
                            if (nbFollower > maxFollower) {
                                maxFollower = nbFollower;
                            }
                            nbFollowerTotal += nbFollower;
                        }
                        for(Key user: database.getOriginalUsers()){
                            Set followers = database.getMapFollowers().get(user);
                            nbFollower = followers.size();

                            if (nbFollower>= maxFollower*0.9)
                                userWithMaxFollower++;
                            else if (nbFollower == 0)
                                userWithoutFollower++;
                        }

                        allAvgQueueSizes.add((int) ((queueSizes.longValue()/ NB_USERS)/nbCurrThread));
                        allAvgFollower.add(nbFollowerTotal/NB_USERS);
                        allNbMaxFollower.add(maxFollower);
                        allNbUserWithMaxFollower.add(userWithMaxFollower);
                        allNbUserWithoutFollower.add(userWithoutFollower);
                    }
                    executor.shutdown();
                }

                if (_gcinfo)
                    System.out.println("benchmarkAvgTime : " + (benchmarkAvgTime / 1_000_000)/_nbTest + "ms");

                long nbOpTotal = 0, timeTotalComputed = 0;

                int unit = nbCurrThread;
//                int unit = NB_USERS;

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
//                String strAlpha = Integer.toString(nbCurrThread);

//              long avgTimeTotal = timeTotal / nbCurrThread; // Compute the avg time to get the global throughput

                if (_s){

                    String nameFile = "ALL_"+_tag+"_"+strAlpha+".txt";
                    if (flag_append == 0)
                        fileWriter = new FileWriter(nameFile, false);
                    else
                        fileWriter = new FileWriter(nameFile, true);

                    printWriter = new PrintWriter(fileWriter);
                    if (_completionTime)
                        printWriter.println(unit +" "+ timeTotal);
                    else
                        printWriter.println(unit +" "+ (nbOpTotal / (double) timeTotalComputed) * 1_000_000_000);

                }

                if (_p){
                    for (int j = 0; j < nbSign; j++) System.out.print("-");
                    if (_completionTime) {
                        System.out.print(" ==> Completion time for " + _nbOps + " operations : ");
                        System.out.println(timeTotal/1_000_000_000 +" seconds");
                    }
                    else {
                        System.out.print(" ==> Throughput (op/s) for all operations : ");
                        System.out.println( String.format("%.3E",(nbOpTotal / (double) timeTotalComputed) * 1_000_000_000));
                        System.out.println(" ==> - temps d'execution : "+ (timeTotalComputed/nbCurrThread)/1_000_000 + "ms");
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

                        String nameFile = mapIntOptoStringOp.get(op)+"_"+_tag+"_"+strAlpha+".txt";
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

                    if (_breakdown){

                        int sumAvgQueueSizes = 0,
                                sumAvgFollower = 0,
                                sumNbMaxFollower = 0,
                                sumNbUserWithMaxFollower = 0,
                                sumNbUserWithoutFollower = 0;

                        for (int i = 0; i < _nbTest; i++) {
                            sumAvgQueueSizes += allAvgQueueSizes.get(i);
                            sumAvgFollower += allAvgFollower.get(i);
                            sumNbMaxFollower += allNbMaxFollower.get(i);
                            sumNbUserWithMaxFollower += allNbUserWithMaxFollower.get(i);
                            sumNbUserWithoutFollower += allNbUserWithoutFollower.get(i);

                        }
                        if (_p){
                            for (int op: mapIntOptoStringOp.keySet()) {
                                int nbSpace = 10 - mapIntOptoStringOp.get(op).length();
                                System.out.print("==> - " + mapIntOptoStringOp.get(op));
                                for (int i = 0; i < nbSpace; i++) System.out.print(" ");
                                System.out.println(": Nb op : " + nbOperations.get(op).get()
                                        + ", proportion : " + (int) ((nbOperations.get(op).get() / (double) nbOpTotal) * 100) + "%"
                                        + ", temps d'exécution : " + (timeOperations.get(op).get()/nbCurrThread) / 1_000_000 + " milli seconds");
                            }

                            System.out.println(" ==> nb original users : " + NB_USERS);
                            System.out.println(" ==> avg queue size : " + sumAvgQueueSizes/_nbTest);
                            System.out.println(" ==> avg follower : " + sumAvgFollower/_nbTest);
                            System.out.println(" ==> nb max follower : " + sumNbMaxFollower/_nbTest);
                            System.out.println(" ==> nb user with max follower (or 10% less) : " + sumNbUserWithMaxFollower/_nbTest);
                            System.out.println(" ==> nb user without follower : " + sumNbUserWithoutFollower/_nbTest);
                            System.out.println();
//                            System.out.println("Map Follower : " + database.getMapFollowers());
//                            System.out.println("Map Following : " + database.getMapFollowing());
                        }

                        if (_gcinfo){
                            long timeBenchmarkAvg = ((timeBenchmark.longValue() / 1_000_000) / nbCurrThread) / _nbTest;
                            System.out.println("Avg benchmark time (without warmup) : " + timeBenchmarkAvg + "ms");
                        }

                        if (_s){
                            FileWriter queueSizeFile, avgFollowerFile, nbMaxFollowerFile, nbUserWithMaxFollowerFile, nbUserWithoutFollowerFile;
                            PrintWriter queueSizePrint, avgFollowerPrint, nbMaxFollowerPrint, nbUserWithMaxFollowerPrint, nbUserWithoutFollowerPrint;

                            if (flag_append == 0) {
                                queueSizeFile = new FileWriter("avg_queue_size_" + _tag + ".txt", false);
                                avgFollowerFile = new FileWriter("avg_Follower_" + _tag + ".txt", false);
                                nbMaxFollowerFile = new FileWriter("nb_Max_Follower_" + _tag + ".txt", false);
                                nbUserWithMaxFollowerFile = new FileWriter("nb_User_With_Max_Follower_" + _tag + ".txt", false);
                                nbUserWithoutFollowerFile = new FileWriter("nb_User_Without_Follower_" + _tag + ".txt", false);
                            }
                            else {
                                queueSizeFile = new FileWriter("avg_queue_size_" + _tag + ".txt", true);
                                avgFollowerFile = new FileWriter("avg_Follower_" + _tag + ".txt", true);
                                nbMaxFollowerFile = new FileWriter("nb_Max_Follower_" + _tag + ".txt", true);
                                nbUserWithMaxFollowerFile = new FileWriter("nb_User_With_Max_Follower_" + _tag + ".txt", true);
                                nbUserWithoutFollowerFile = new FileWriter("nb_User_Without_Follower_" + _tag + ".txt", true);
                            }

                            queueSizePrint = new PrintWriter(queueSizeFile);
                            avgFollowerPrint = new PrintWriter(avgFollowerFile);
                            nbMaxFollowerPrint = new PrintWriter(nbMaxFollowerFile);
                            nbUserWithMaxFollowerPrint = new PrintWriter(nbUserWithMaxFollowerFile);
                            nbUserWithoutFollowerPrint = new PrintWriter(nbUserWithoutFollowerFile);

                            queueSizePrint.println(unit + " " + sumAvgQueueSizes/_nbTest);
                            avgFollowerPrint.println(unit + " " + sumAvgFollower/_nbTest);
                            nbMaxFollowerPrint.println(unit + " " + sumNbMaxFollower/_nbTest);
                            nbUserWithMaxFollowerPrint.println(unit + " " + sumNbUserWithMaxFollower/_nbTest);
                            nbUserWithoutFollowerPrint.println(unit + " " + sumNbUserWithoutFollower/_nbTest);

                            queueSizePrint.flush();
                            avgFollowerPrint.flush();
                            nbMaxFollowerPrint.flush();
                            nbUserWithMaxFollowerPrint.flush();
                            nbUserWithoutFollowerPrint.flush();

                            queueSizeFile.close();
                            avgFollowerFile.close();
                            nbMaxFollowerFile.close();
                            nbUserWithMaxFollowerFile.close();
                            nbUserWithoutFollowerFile.close();
                        }
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

        private final ThreadLocalRandom random;
        private final int[] ratiosArray;
        private final CountDownLatch latch;
        private final CountDownLatch latchFillDatabase;
        private Map<Key, Queue<Key>> usersFollow; // Local map that associate to each user, the list of user that it follows
        private Integer usersProbabilitySize;
        private List<Key> arrayLocalUsers; // Local array that store the users handled by a thread, a user is put n times following a powerlaw
        private int nbRepeat = 1000;
        private final String msg = "new msg";
        int n, nbLocalUsers, nbAttempt;
        Key userB, userA;
	    long startTime, endTime;
        Map<Integer, BoxedLong> nbLocalOperations;
        Map<Integer, BoxedLong> timeLocalOperations;

        public RetwisApp(CountDownLatch latch,CountDownLatch latchFillDatabase) {
            this.random = ThreadLocalRandom.current();
            this.ratiosArray = Arrays.stream(distribution).mapToInt(Integer::parseInt).toArray();
            this.latch = latch;
            this.latchFillDatabase = latchFillDatabase;
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

                database.fill(NB_USERS, latchFillDatabase, usersFollow);

                latch.countDown();
                latch.await();

//                usersProbabilitySize = database.getLocalUsersProbability().get().size();
                usersProbabilitySize = database.getUsersProbability().size();
                arrayLocalUsers = database.getLocalUsers().get();

                while (flagWarmingUp.get()) { // warm up
                    type = chooseOperation();
                    compute(type, nbLocalOperations, timeLocalOperations);
                }

                long startTimeBenchmark, endTimeBenchmark;

                startTimeBenchmark = System.nanoTime();
                if (_completionTime){
                    for (int i = 0; i < _nbOps/_nbThreads; i++) {
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

                    for (Key user : database.getLocalUsers().get()){
                        queueSizes.add(database.getMapTimelines().get(user).getTimeline().size());
                    }
                }

                endTimeBenchmark = System.nanoTime();

                timeBenchmark.add(endTimeBenchmark - startTimeBenchmark);

                for (int op: mapIntOptoStringOp.keySet()){
                    nbOperations.get(op).addAndGet(nbLocalOperations.get(op).val);
                    timeOperations.get(op).addAndGet(timeLocalOperations.get(op).val);
                }

            } catch (InterruptedException | InvocationTargetException | IllegalAccessException | ClassNotFoundException | InstantiationException | NoSuchMethodException e) {
                e.printStackTrace();
            }
            return null;
        }

        public void dummyFunction() throws InterruptedException {
            TimeUnit.MICROSECONDS.sleep(1000);
        }

        public int chooseOperation(){
            int type;

            int val = random.nextInt(100);
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

            nbLocalUsers = arrayLocalUsers.size();
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
                nbAttempt ++;
                if (nbAttempt > nbAttemptMax)
                    typeComputed = chooseOperation();

                int val = random.nextInt(nbLocalUsers);
                userA = arrayLocalUsers.get(val);
                Queue<Key> listFollow = usersFollow.get(userA);
                switch (typeComputed){
                    case ADD:
                        if (_completionTime){
                            database.addUser();
                        }else{
                            startTime = System.nanoTime();
//                            database.addUser();
                            endTime = System.nanoTime();
                        }
                        break;
                    case FOLLOW:
                        n = random.nextInt(usersProbabilitySize); // We choose a user to follow according to a probability
//                        userB = database.getLocalUsersProbability().get().get(n);
                        userB = database.getUsersProbability().get(n);

                        try{
                            if (!listFollow.contains(userB)){ // Perform follow only if userB is not already followed
                                if (_completionTime){
                                    database.followUser(userA, userB);
                                }else {
                                    startTime = System.nanoTime();
//                                    database.followUser(userA, userB);
                                    endTime = System.nanoTime();
                                }
                                listFollow.add(userB);
                            }else
                                continue restartOperation;
                        }catch (NullPointerException e){
//                        System.out.println(userA + " may not have a list of follow (Follow method)");
//                        Make a "debug mode" to specify when a process doesn't handle userA
                        }
                        break;
                    case UNFOLLOW:
                        try{
                            userB = listFollow.poll();
                            if (userB != null){ // Perform unfollow only if userA already follow someone
                                if (_completionTime){
                                    database.unfollowUser(userA, userB);
                                }else{
                                    startTime = System.nanoTime();
//                                    database.unfollowUser(userA, userB);
                                    endTime = System.nanoTime();
                                }
                            }else
                                continue restartOperation;
                        }catch (NullPointerException e){
//                        System.out.println(userA + " may not have a list of follow (Unfollow method)");
                        }
                        break;
                    case TWEET:
                        if (_completionTime){
                            database.tweet(userA, msg);
                        }else{
                            startTime = System.nanoTime();
//                            database.tweet(userA, msg);
                            endTime = System.nanoTime();
                        }
                        break;
                    case READ:
                        if (_completionTime){
                            database.showTimeline(userA);
                        }else{
                            startTime = System.nanoTime();
//                            database.showTimeline(userA);
                            endTime = System.nanoTime();
                        }
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + type);
                }

                if (!flagWarmingUp.get() && !_completionTime) {
                    nbOps.get(typeComputed).val += 1;
                    timeOps.get(typeComputed).val+= endTime - startTime;
                }

                break;
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

                if (! _completionTime) {
                    if (_p)
                        System.out.println(" ==> Computing the throughput for "+ _time +" seconds");
                    if (_gcinfo) {
                        System.out.println("Start benchmark");
                    }
                    TimeUnit.SECONDS.sleep(_time);
                    flagComputing.set(false);
                    if (_gcinfo) {
                        System.out.println("End benchmark");
                    }
                }else{
                    if (_p)
                        System.out.println(" ==> Computing the completion time for " + _nbOps + " operations");
                }
            } catch (InterruptedException e) {
                throw new Exception("Thread interrupted", e);
            }
            return null;
        }
    }
}
