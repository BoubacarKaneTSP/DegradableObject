package eu.cloudbutton.dobj.benchmark;
import eu.cloudbutton.dobj.Factory;
import eu.cloudbutton.dobj.Timeline;
import eu.cloudbutton.dobj.incrementonly.BoxedLong;
import eu.cloudbutton.dobj.incrementonly.Counter;
import eu.cloudbutton.dobj.incrementonly.CounterIncrementOnly;
import eu.cloudbutton.dobj.key.Key;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

public class Retwis {

    private static final int ADD = 0, FOLLOW = 1, UNFOLLOW = 2, TWEET = 3, READ = 4, COUNT = 5, GROUP = 6, PROFILE = 7;
    private static final Map<Integer, String> mapIntOptoStringOp = new HashMap<>(){{
        put(ADD, "ADD");
        put(FOLLOW, "FOLLOW");
        put(UNFOLLOW, "UNFOLLOW");
        put(TWEET, "TWEET");
        put(READ, "READ");
        put(COUNT, "COUNT");
        put(GROUP, "GROUP");
        put(PROFILE, "PROFILE");
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

    private List<Counter> nbOperations;
    private List<AtomicLong> timeOperations;
    private Map<Integer, List<Long>> timeDurations;
    private AtomicInteger timeBenchmark;
    private Queue<String> userUsageDistribution;
    private LongAdder queueSizes;
//    private Long nbUserFinal;
//    private Long nbTweetFinal;
    private List<Float> allAvgQueueSizes;
//    private List<Float> allAvgFollower;
//    private List<Float> allAvgFollowing;
//    private List<Float> allProportionMaxFollower;
//    private List<Float> allProportionMaxFollowing;
//    private List<Float> allProportionUserWithMaxFollower;
//    private List<Float> allProportionUserWithMaxFollowing;
//    private List<Float> allProportionUserWithoutFollower;
//    private List<Float> allProportionUserWithoutFollowing;

    private Database database;

    int NB_USERS;

    int nbSign = 5;

    int flag_append = 1;

    private long completionTime;

    public static void main(String[] args) throws InterruptedException, IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, ExecutionException {
        new Retwis().doMain(args);
    }

    public void doMain(String[] args) throws InterruptedException, IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, OutOfMemoryError, ExecutionException {
        CmdLineParser parser = new CmdLineParser(this);

        try{
            parser.parseArgument(args);

            if (args.length < 1)
                throw new CmdLineException(parser, "No argument is given");

            if (distribution.length != 6){
                throw new java.lang.Error("#ratios must be 6 (% add, % follow or unfollow, % tweet, % read, % join/leave groupe, % update profile), has:"+Arrays.toString(distribution));
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

        listAlpha.add(_alphaMin);

        NB_USERS = (int) _nbUserInit;

        if (_nbUserInit > _nbItems){
            System.out.println("Nb User must be lower or equal to number of hash");
            System.exit(1);
        }

        for (int nbCurrThread = _nbThreads; nbCurrThread <= _nbThreads;) {

            if (_gcinfo) {
                System.out.println("nbThread : "+nbCurrThread);
            }

            flag_append = nbCurrThread == 1 ? 0 : 1;

            PrintWriter printWriter = null;
            FileWriter fileWriter;
            String nameFile;

            allAvgQueueSizes = new ArrayList();
//            allAvgFollower = new ArrayList();
//            allAvgFollowing = new ArrayList();
//            allProportionMaxFollower = new ArrayList();
//            allProportionMaxFollowing = new ArrayList();
//            allProportionUserWithMaxFollower = new ArrayList();
//            allProportionUserWithMaxFollowing = new ArrayList();
//            allProportionUserWithoutFollower = new ArrayList();
//            allProportionUserWithoutFollowing = new ArrayList();

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
                userUsageDistribution = new ConcurrentLinkedQueue<>();
//                timeDurations = new ConcurrentHashMap<>();
                queueSizes = new LongAdder();
//                nbUserFinal = 0L;
//                nbTweetFinal = 0L;
                timeBenchmark = new AtomicInteger();
                completionTime = 0;


                for (int op: mapIntOptoStringOp.keySet()) {
//                    nbOperations.add(op, Factory.createCounter(typeCounter));
                    nbOperations.add(op, new CounterIncrementOnly());
                    timeOperations.add(op, new AtomicLong());
//                    timeDurations.put(op, new CopyOnWriteArrayList<>());
                }

                for (int nbCurrTest = 1; nbCurrTest <= _nbTest; nbCurrTest++) {

                    List<Callable<Void>> callables = new ArrayList<>();
                    flagComputing = new AtomicBoolean(true);
                    flagWarmingUp = new AtomicBoolean(false);

                    database = new Database(typeMap, typeSet, typeQueue, typeCounter,
                            nbCurrThread,
                            NB_USERS,
                            alpha
                    );

                    if (nbCurrTest == 1){
                        flagWarmingUp.set(true);
                    }

                    CountDownLatch latchCompletionTime = new CountDownLatch(nbCurrThread+1);// Additional counts for the coordinator
                    CountDownLatch latchFillDatabase = new CountDownLatch(nbCurrThread);
                    CountDownLatch latchFillFollowingPhase = new CountDownLatch(nbCurrThread);
                    CountDownLatch computePhase = new CountDownLatch(nbCurrThread);

                    for (int j = 0; j < nbCurrThread; j++) {
                        RetwisApp retwisApp = new RetwisApp(
                                latchCompletionTime,
                                latchFillDatabase,
                                latchFillFollowingPhase,
                                computePhase
                        );
                        callables.add(retwisApp);
                    }

                    List<Future<Void>> futures = new ArrayList<>();
                    futures.add(Executors.newFixedThreadPool(1).submit(
                            new Coordinator(latchCompletionTime, latchFillDatabase, latchFillFollowingPhase)));
                    futures.addAll(database.getExecutorService().invokeAll(callables));

                    try{
                        for (Future<Void> future : futures) {
                            future.get();
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                        System.exit(0);
                    }



                    if(_p)
                        System.out.println(" ==> End of test num : " + nbCurrTest);

                    TimeUnit.SECONDS.sleep(1);

                    if (_breakdown){
                        allAvgQueueSizes.add( (((float)queueSizes.intValue()/ NB_USERS)/nbCurrThread));
                    }
                    database.shutdown();
                }

                if(_p)
                    System.out.println();

                if (_gcinfo || _p) {
                    System.out.println("completion time : " + (double) completionTime / (double) 1_000_000_000 +" sec.");
                    System.out.print(database.statistics());
                }

                long nbOpTotal = 0, timeTotalComputed = 0;

                int unit = nbCurrThread;

                for (int op: mapIntOptoStringOp.keySet()) {
                    nbOpTotal += nbOperations.get(op).read();
                    timeTotalComputed += timeOperations.get(op).get();
                }

                if (_breakdown && !_completionTime){

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
                    }
                    if (_p){
                        System.out.println("Stats for each op over (" + _nbTest + ") tests :");
                        for (int op: mapIntOptoStringOp.keySet()) {
                            int nbSpace = 10 - mapIntOptoStringOp.get(op).length();
                            System.out.print("==> - " + mapIntOptoStringOp.get(op));
                            for (int i = 0; i < nbSpace; i++) System.out.print(" ");
                            System.out.println(": #ops : " + nbOperations.get(op).read()
                                    + ", proportion : " + (int) ((nbOperations.get(op).read() / (double) nbOpTotal) * 100) + "%"
                                    + ", time spent : " + (timeOperations.get(op).get()/nbCurrThread) / 1_000 + " us");
                        }

                        System.out.println("[Total (op): " + nbOpTotal);
                        System.out.printf("[Throughput (op/s): %.3E]", + nbOpTotal/(completionTime/(double)1000000000));
//                        System.out.println(" ==> avg sum time op : " + ((timeTotalComputed/1_000_000)/nbCurrThread)/_nbTest + " ms");
//                        System.out.println(" ==> nb original users : " + NB_USERS);
//                        System.out.println(" ==> nb Tweet at the end : " + nbTweetFinal/_nbTest);
                        System.out.println(" ==> avg queue size : " + sumAvgQueueSizes/_nbTest);
                        System.out.println(" ==> avg follower : " + sumAvgFollower/_nbTest);
                        System.out.println(" ==> avg following : " + sumAvgFollowing/_nbTest);
                        System.out.println(" ==> % of the database that represent the max number of follower : " + sumProportionMaxFollower/_nbTest + "%");
                        System.out.println(" ==> % of the database that represent the max number of following : " + sumProportionMaxFollowing/_nbTest + "%");
                        System.out.println(" ==> % user with max follower (or 20% less) : " + sumProportionUserWithMaxFollower/_nbTest + "%");
                        System.out.println(" ==> % user with max following (or 20% less) : " + sumProportionUserWithMaxFollowing/_nbTest + "%");
                        System.out.println(" ==> % user without follower : " + sumProportionUserWithoutFollower/_nbTest + "%");
                        System.out.println(" ==> % user without following : " + sumProportionUserWithoutFollowing/_nbTest + "%");
//                        System.out.println(" ==> nb user at the end : " + nbUserFinal/_nbTest);
                        System.out.println();
                    }

                }

                if(_p)
                    System.out.println();

            }

            nbCurrThread *= 2;

            if (_quickTest){
                if(nbCurrThread==2)
                    nbCurrThread = _nbThreads;
            }

            if (nbCurrThread > _nbThreads && nbCurrThread != 2 * _nbThreads)
                nbCurrThread = _nbThreads;

        }
        // System.out.println("closing prog");
        System.exit(0);
    }

    public class RetwisApp implements Callable<Void>{

        private static final int MAX_USERS_PER_THREAD = 100_000;

        private final ThreadLocalRandom random;
        private final int[] ratiosArray;
        private final CountDownLatch latchFillCompletionTime;
        private final CountDownLatch latchFillDatabase;
        private final CountDownLatch latchFillFollowingPhase;
        private final CountDownLatch computePhase;
        private Long localUsersUsageProbabilityRange;
        private Long usersFollowProbabilityRange;
        private Queue<String> localUserUsageDistribution;
        private final String msg = "new msg";
        AtomicInteger counterID;
        private final ThreadLocal<Integer> myId;
        int nbLocalUsers;
        int nbAttempt;
        List<Key> users, usersToFollow;
        Key user, userToFollow;
        int nextUser, nextUserToFollow;
        Key dummyUser;
        Key dummyUserFollow;
        Set<Key> dummySet;
        Timeline<String> dummyTimeline;
        List<Integer> listOperationToDo;

        public RetwisApp(CountDownLatch latchFillCompletionTime, CountDownLatch latchFillDatabase, CountDownLatch latchFollowingPhase, CountDownLatch computePhase) {
            this.random = ThreadLocalRandom.current();
            this.myId = new ThreadLocal<>();
            this.ratiosArray = Arrays.stream(distribution).mapToInt(Integer::parseInt).toArray();
            this.latchFillCompletionTime = latchFillCompletionTime;
            this.latchFillDatabase = latchFillDatabase;
            this.latchFillFollowingPhase = latchFollowingPhase;
            this.computePhase = computePhase;
            this.counterID = new AtomicInteger();
        }

        @Override
        public Void call(){

            try{
                int type;
                int sizeOpToDo = 10_000;

                myId.set(database.getCount().getAndIncrement());

                for (Key user : database.getMapUserToAdd().get(myId.get())){
                    database.addOriginalUser(user);
                }
//                System.out.println("Thread num : " + myId.get() + " manage : " + database.getMapUserToAdd().get(myId.get()));

                latchFillDatabase.countDown();
                latchFillDatabase.await();

//                System.out.println("done filling");

                for (Key userA : database.getMapUserToAdd().get(myId.get())){
//                    System.out.print(userA + " follow : ");
                    for (Key userB : database.getMapListUserFollow().get(userA)){
//                        System.out.print(userB + ", ");
                        try{

                            database.followUser(userA, userB);
                        } catch (NullPointerException e) {

//                            System.out.println("userA : " + userA + " | userB : " + userB);

                            e.printStackTrace();

                            System.out.println("is "+ userB +" one of Thread "+ myId.get() +" user : " + database.getMapUserToAdd().get(myId.get()).contains(userB));
                            System.out.println("Thread " + myId.get() + " | Map follower of user : " + userB + " " + database.getMapFollowers().get(userB) + " | groupe of user : " + database.getMapUserToAdd().get(myId.get()));

                            System.exit(1);
                        }
                    }
//                    System.out.println();
//                    System.out.println();
                }

                latchFillFollowingPhase.countDown();
                latchFillFollowingPhase.await();
//                System.out.println("done following");

                Map<Integer, BoxedLong> timeLocalOperations = new HashMap<>();
//                Map<Integer, List<Long>> timeLocalDurations = new HashMap<>();

                for (int op: mapIntOptoStringOp.keySet()){
                    timeLocalOperations.put(op, new BoxedLong());
//                    timeLocalDurations.put(op, new ArrayList<>());
                }

                localUsersUsageProbabilityRange = database.getLocalUsersUsageProbabilityRange().get(myId.get());
                usersFollowProbabilityRange = database.getUsersFollowProbabilityRange();
                nbLocalUsers = database.getMapUserToAdd().get(myId.get()).size();
                localUserUsageDistribution = new LinkedList<>();

                listOperationToDo = new ArrayList<>();

                for (int i = 0; i < sizeOpToDo; i++) {
                    listOperationToDo.add(chooseOperation());
                }

                dummyUser = database.generateUser();
                dummySet = new HashSet<>();
                dummyTimeline = new Timeline<>(new LinkedList<>());

                dummyUserFollow = database.generateUser();

                database.addOriginalUser(dummyUserFollow);
                int num = 0;
                boolean cleanTimeline = false;

                users = new ArrayList<>(MAX_USERS_PER_THREAD);
                usersToFollow = new ArrayList<>(MAX_USERS_PER_THREAD);
                for (int i=0; i<MAX_USERS_PER_THREAD; i++) {
                    long val = Math.abs(random.nextLong() % (localUsersUsageProbabilityRange + 1));
                    users.add(database
                            .getLocalUsersUsageProbability()
                            .get(myId.get())
                            .ceilingEntry(val)
                            .getValue());
                    val = Math.abs(random.nextLong() % (usersFollowProbabilityRange + 1));
                    usersToFollow.add(database
                            .getUsersFollowProbability()
                            .ceilingEntry(val)
                            .getValue());
                }
                nextUser = 0;
                nextUserToFollow = 0;

                while (flagWarmingUp.get()) { // warm up
                    type = chooseOperation();
                    compute(type, timeLocalOperations, cleanTimeline,num);
                }

                computePhase.countDown();
                computePhase.await();

                long startTimeBenchmark, endTimeBenchmark;

                startTimeBenchmark = System.nanoTime();
                if (_completionTime){
                    long nbOperationToDo = Math.ceilDiv( _nbOps, database.getNbThread());
                    for (long i = 0; i < nbOperationToDo; i++) {
//                        dummyFunction();
                        type = chooseOperation();
                        compute(type, timeLocalOperations, cleanTimeline, i);
//                        cleanTimeline = i % (2 * _nbUserInit) == 0;
                    }
                }else{

                    num=0;

                    while (flagComputing.get()){

                        type = chooseOperation();
                        // type = listOperationToDo.get(num%sizeOpToDo);

                        if (_multipleOperation){
                            int nbRepeat = 1000;
                            for (int j = 0; j < nbRepeat; j++) {
                                compute(type, timeLocalOperations, cleanTimeline,num);
//                                compute(type, timeLocalOperations, timeLocalDurations, false,num);
                                cleanTimeline = num++ % (2 * _nbUserInit) == 0;
//                                num++;
                            }
                        }else{

                            compute(type, timeLocalOperations, cleanTimeline, num);
//                            compute(type, timeLocalOperations, timeLocalDurations, false, num);
                            num++;
//
//                            cleanTimeline = num++ % (2 * _nbUserInit) == 0;
                        }
                    }
                }

                endTimeBenchmark = System.nanoTime();

                if (_completionTime){
                    latchFillCompletionTime.countDown();
                    latchFillCompletionTime.await();
                }

                timeBenchmark.addAndGet((int) (endTimeBenchmark - startTimeBenchmark));

                if (!_completionTime){
                    for (int op: mapIntOptoStringOp.keySet()){
//                    nbOperations.get(op).addAndGet(nbLocalOperations.get(op).val);
                        timeOperations.get(op).addAndGet(timeLocalOperations.get(op).val);
//                    timeDurations.get(op).addAll(timeLocalDurations.get(op));
                    }
                }

//                userUsageDistribution.addAll(localUserUsageDistribution);

            } catch (Throwable e) {
                e.printStackTrace();
                throw new Error(e);
            }
            return null;
        }

        public void dummyFunction() throws InterruptedException {
            nbAttempt++;
//            TimeUnit.NANOSECONDS.sleep(1);
//            System.nanoTime();
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
            }else if(val >= ratiosArray[0] + ratiosArray[1] + ratiosArray[2] && val < ratiosArray[0]+ ratiosArray[1]+ ratiosArray[2] + ratiosArray[3]){
                type = READ;
            }else if(val >= ratiosArray[0] + ratiosArray[1] + ratiosArray[2] + ratiosArray[3] && val < ratiosArray[0]+ ratiosArray[1]+ ratiosArray[2] + ratiosArray[3] + ratiosArray[4]){
                type = GROUP;
            }else{
                type = PROFILE;
            }

            return type;
        }

//        public void compute(int type, Map<Integer, BoxedLong> timeOps, Map<Integer, List<Long>> timeLocalDurations, boolean cleanTimeline, int numOperation) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, ClassNotFoundException, InstantiationException, InterruptedException {
        public void compute(int type, Map<Integer, BoxedLong> timeOps, boolean cleanTimeline, long numOperation) {


            try {

                // startTime = 0L;
                // endTime= 0L;
//                nbAttempt = -1;

                //            int nbAttemptMax = (int) (Math.log(0.01)/Math.log((nbLocalUsers-1) / (double) nbLocalUsers));
                //            int nbAttemptMax = 10;

                int typeComputed = type;

                for (; ; ) {
                    user = users.get(nextUser++ % MAX_USERS_PER_THREAD);
                    switch (typeComputed) {
                        case ADD:
                            database.addUser(dummyUser, dummySet, dummyTimeline);
                            database.removeUser(dummyUser);
                            break;
                        case FOLLOW:
                        case UNFOLLOW:
                            userToFollow = usersToFollow.get(nextUserToFollow++ % MAX_USERS_PER_THREAD);
                            database.followUser(user, userToFollow);
                            database.unfollowUser(user, userToFollow);
                            break;
                        case TWEET:
                            // startTime = System.nanoTime();
                            database.tweet(user, msg);
                            // endTime = System.nanoTime();
                            break;
                        case PROFILE:
                            // startTime = System.nanoTime();
                            database.updateProfile(user);
                            // endTime = System.nanoTime();
                            break;
                        case READ:
                            // startTime = System.nanoTime();
                            database.showTimeline(user);
                            // endTime = System.nanoTime();
                            break;
                        case GROUP:
                            if (database.getMapCommunityStatus().get(user) == 0) {
                                database.getMapCommunityStatus().put(user, 1);
                                // startTime = System.nanoTime();
                                database.joinCommunity(user);
                            } else {
                                database.getMapCommunityStatus().put(user, 0);
                                // startTime = System.nanoTime();
                                database.leaveCommunity(user);

                            }
                            // endTime = System.nanoTime();
                            break;
                        default:
                            throw new IllegalStateException("Unexpected value: " + type);
                    }

                    //                    startTime = System.nanoTime();
                    //                    typeComputed = COUNT;
                    //                    for (int i = 0; i < 1000; i++) {
                    //                        // System.out.println("here w."+Thread.currentThread().getName());
                    //                        database.getCounter().incrementAndGet();
                    //                    }
                    //                    endTime = System.nanoTime();

                    if (!flagWarmingUp.get() && !_completionTime) {
                        // timeOps.get(typeComputed).val+= endTime - startTime;
                        //                        timeLocalDurations
                        //                                .get(typeComputed)
                        //                                .add(endTime - startTime);

                        // startTime = System.nanoTime();
                        nbOperations.get(typeComputed).addAndGet(1);
                        //                        endTime = System.nanoTime();
                        //                        timeOps.get(COUNT).val += endTime - startTime;
                        //                        timeLocalDurations.get(COUNT).add(endTime - startTime);
                        //                        System.out.println(timeLocalDurations.get(COUNT).size());

                    }

                    // Thread.sleep(0,1); // simulate I/O

                    break;
                    //                }
                }

//                System.out.println("user="+user+ "; op="+typeComputed);
//                Thread.sleep(1000);


            }catch (Throwable e) {
                e.printStackTrace();
                // System.err.println(Thread.currentThread()+": "+user);
                throw new RuntimeException();
            }

        }

        public void resetAllTimeline(){
            for (Key usr: database.getLocalUsersUsageProbability().get(myId.get()).values()){
                database.getMapTimelines().get(usr).clear();
            }
        }
    }

    public class Coordinator implements Callable<Void> {

        private final CountDownLatch latchCompletionTime;
        private final CountDownLatch latchFillDatabase;
        private final CountDownLatch latchFillFollowingPhase;

        public Coordinator(CountDownLatch latchCompletionTime, CountDownLatch latchFillDatabase, CountDownLatch latchFillFollowingPhase) {
            this.latchCompletionTime = latchCompletionTime;
            this.latchFillDatabase = latchFillDatabase;
            this.latchFillFollowingPhase = latchFillFollowingPhase;
        }

        @Override
        public Void call() throws Exception {
            long startTime, endTime;
            try {

                if (_p)
                    System.out.println(" ==> Filling the database with "+ NB_USERS +" users" );

                latchFillDatabase.await();
                latchFillFollowingPhase.await();

                if (flagWarmingUp.get()){

//                    latchHistogram.await();

//                    saveDistributionHistogram("Pre_Benchmark");
//                    System.out.println("Average coefficient cluster with biased : " + database.computeAvgCoefficientCluster());

//                    performHeapDump(_tag, "Pre", (int) _nbUserInit);


                    if (_p){
                        System.out.println(" ==> Warming up for " + _wTime + " seconds");
                    }

                    TimeUnit.SECONDS.sleep(_wTime);

                    flagWarmingUp.set(false);
                }

                if (_gcinfo)
                    System.out.println("Start benchmark");

                if (! _completionTime) {
                    if (_p) {
                        System.out.println(" ==> Computing the throughput for "+ _time +" seconds");
                    }

                    startTime = System.nanoTime();
                    TimeUnit.SECONDS.sleep(_time);
                    flagComputing.set(false);
                    endTime = System.nanoTime();
                    completionTime += endTime - startTime;

                }else{

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

            } catch (Throwable e) {
                e.printStackTrace();
                throw new Error(e);
            }
            return null;
        }

        private void saveOperationDistribution() throws IOException {
            System.out.println("Save operation duration distribution");

            Map<Long, Integer> map;

            int binSize = 10000;

            PrintWriter printWriter;
            FileWriter fileWriter;

            for (int type: mapIntOptoStringOp.keySet()){
                fileWriter = new FileWriter("Duration_"+mapIntOptoStringOp.get(type)+"_Distribution_"+ _tag + "_" + _nbUserInit + "_Users_" + _nbThreads + "_Threads.txt", false);
                printWriter = new PrintWriter(fileWriter);

                map = new HashMap<>();

                for(Long time : timeDurations.get(type)){
                    time = time - time % binSize;

                    if (!map.containsKey(time)) {
                        map.put(time,1);
                    }
                    else {
                        map.put(time, map.get(time)+1);
                    }
                }

                for (Long val: map.keySet()){
                    printWriter.println(val + " " + map.get(val));
                }

                printWriter.flush();
                fileWriter.close();
            }

        }

        private void saveUserUsageDistribution() throws IOException {
            System.out.println("Save user usage distribution");

            Map<String, Integer> map = new HashMap<>();

            PrintWriter printWriter;
            FileWriter fileWriter;

            fileWriter = new FileWriter("User_Usage_Distribution_"+ _tag + "_" + _nbUserInit + "_Users_" + _nbThreads + "_Threads.txt", false);
            printWriter = new PrintWriter(fileWriter);

            for (String s : userUsageDistribution){
                if (!map.containsKey(s)) {
                    map.put(s,1);
                }
                else {
                    map.put(s, map.get(s)+1);
                }
            }

            for (String s: map.keySet()){
                printWriter.println(s + " " + map.get(s));
            }

            printWriter.flush();
            fileWriter.close();
        }

        private void saveDistributionHistogram(String tag) throws IOException {
            if (_p){
                System.out.println("Saving "+ tag +" distribution histogram");
            }

            int range = 5;
            int max = 100;
            String distributionHistogramFollower, distributionHistogramFollowing;

//            distributionHistogramFollower = database.computeHistogram(range, max,"Follower");
//            distributionHistogramFollowing = database.computeHistogram(range, max,"Following");

            Map<Integer,Integer> mapHistogramFollower, mapHistogramFollowing;
//
            mapHistogramFollower = database.computeFollowHistogram(range, max,"Follower");
            mapHistogramFollowing = database.computeFollowHistogram(range, max,"Following");

            System.out.println("done computing the map");
            PrintWriter printWriter;
            FileWriter fileWriter;

            fileWriter = new FileWriter("Follower_Distribution_"+ _tag + "_" + tag + "_" + _nbUserInit + "_Users_" + _nbThreads + "_Threads.txt", false);
            printWriter = new PrintWriter(fileWriter);

//            printWriter.println(distributionHistogramFollower);

            for (Integer k : mapHistogramFollower.keySet())
                printWriter.println(k + " " + mapHistogramFollower.get(k));

            printWriter.flush();
            fileWriter.close();

            fileWriter = new FileWriter("Following_Distribution_" + _tag + "_" + tag + "_" + _nbUserInit + "_Users_" + _nbThreads + "_Threads.txt", false);
            printWriter = new PrintWriter(fileWriter);

//            printWriter.println(distributionHistogramFollowing);
            for (Integer k : mapHistogramFollowing.keySet())
                printWriter.println(k + " " + mapHistogramFollowing.get(k));

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
