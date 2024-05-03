package eu.cloudbutton.dobj.benchmark;
import eu.cloudbutton.dobj.Timeline;
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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

import static eu.cloudbutton.dobj.benchmark.Retwis.operationType.*;

public class Retwis {

    enum operationType {
        ADD,
        REMOVE,
        FOLLOW,
        UNFOLLOW,
        TWEET,
        READ,
        COUNT,
        GROUP,
        PROFILE
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

    @Option(name = "-generate", usage = "If true, generate the graph then exit")
    private boolean _generate = false;


    private AtomicBoolean flagComputing,flagWarmingUp;
    private AtomicLong totalTime;

    private Map<operationType,AtomicInteger> nbOperations;
    private Map<operationType, Queue<Long>> timeDurations;
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
    private long benchmarkTime;

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
            e.printStackTrace();
            throw new RuntimeException();
        }

        if (_p)
            System.out.println(" ==> Launching test from App.java, a clone of Retwis...");

        benchmarkTime = System.nanoTime();

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

                userUsageDistribution = new ConcurrentLinkedQueue<>();
                nbOperations = new ConcurrentHashMap<>();
                Arrays.stream(operationType.values()).forEach(t -> nbOperations.put(t, new AtomicInteger()));
                timeDurations = new ConcurrentHashMap<>();
                Arrays.stream(operationType.values()).forEach(t -> timeDurations.put(t, new ConcurrentLinkedQueue<>()));

//                timeDurations = new ConcurrentHashMap<>();
                queueSizes = new LongAdder();
//                nbUserFinal = 0L;
//                nbTweetFinal = 0L;
                totalTime = new AtomicLong();
                completionTime = 0;

                for (int nbCurrTest = 1; nbCurrTest <= _nbTest; nbCurrTest++) {

                    List<Callable<Void>> callables = new ArrayList<>();
                    flagComputing = new AtomicBoolean(true);
                    flagWarmingUp = new AtomicBoolean(false);

                    database = new Database(typeMap, typeSet, typeQueue, typeCounter,
                            nbCurrThread,
                            NB_USERS,
                            alpha
                    );

                    if (_generate) {
                        database.generateAndSaveGraph();
                        System.out.println("Done, exiting.");
                        System.exit(0);
                    }

                    database.loadGraph();
                    // database.loadCompleteGraph();
                    // database.loadDAPGraph();

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
                        throw new RuntimeException();
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
                    double throughput = Math.ceil((double)(_nbOps * 1_000_000) / (double) (completionTime) );
                    double throughput_per_process = Math.ceil(throughput / _nbThreads);
                    System.out.println("completion time : " + (double) completionTime / (double) 1_000_000_000 + " seconds ("+throughput+" Kops/s, "+throughput_per_process+ "Kops/s per process)");
                    System.out.println("total time : " + (double) totalTime.get() / (double) 1_000_000_000 + " seconds");
                    System.out.println("benchmark time : " + (double) (System.nanoTime()-benchmarkTime) / (double) 1_000_000_000 + " seconds");
                    System.out.print(database.statistics());
                }

                long nbOpTotal = 0, timeTotalComputed = 0;

                int unit = nbCurrThread;

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
                        for (operationType op: operationType.values()) {
                            int nbSpace = 10 - op.toString().length();
                            System.out.print("==> - " + op);
                            for (int i = 0; i < nbSpace; i++) System.out.print(" ");
                            System.out.println(": #ops : " + nbOperations.get(op).get()
                                    + ", proportion : " + (int) ((nbOperations.get(op).get() / (double) nbOpTotal) * 100) + "%");
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
        private static final int MAX_DUMMY_USERS_PER_THREAD = 10_000;
        private static final int MAX_USERS_TO_FOLLOW_PER_THREAD = 1_000;

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
        List<Key> users, usersToFollow, dummies;
        Key user, userToFollow, dummy;
        int nextUser, nextUserToFollow, nextDummy;
        List<operationType> listOperationToDo;

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
                operationType type;
                int sizeOpToDo = 10_000;

                myId.set(database.getCount().getAndIncrement());

                for (Key user : database.getMapUserToAdd().get(myId.get())){
                    database.addOriginalUser(user);
                }

                latchFillDatabase.countDown();
                latchFillDatabase.await();

                if (myId.get()==0) assert database.getMapFollowers().size() == NB_USERS;

                for (Key userA : database.getMapUserToAdd().get(myId.get())){
                    for (Key userB : database.getMapListUserFollow().get(userA)){
                        try{
                            database.followUser(userA, userB);
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                            throw new RuntimeException();
                        }
                    }
                }

                latchFillFollowingPhase.countDown();
                latchFillFollowingPhase.await();

                localUsersUsageProbabilityRange = database.getLocalUsersUsageProbabilityRange().get(myId.get());
                usersFollowProbabilityRange = database.getUsersFollowProbabilityRange();
                nbLocalUsers = database.getMapUserToAdd().get(myId.get()).size();
                localUserUsageDistribution = new LinkedList<>();

                listOperationToDo = new ArrayList<>();

                for (int i = 0; i < sizeOpToDo; i++) {
                    listOperationToDo.add(chooseOperation());
                }


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
                }

                for(int i=0; i<MAX_USERS_TO_FOLLOW_PER_THREAD; i++) {
                    long val = Math.abs(random.nextLong() % (usersFollowProbabilityRange + 1));
                    usersToFollow.add(database
                            .getUsersFollowProbability()
                            .ceilingEntry(val)
                            .getValue());
                }

                dummies = new ArrayList<>();
                for (int i=0; i<MAX_DUMMY_USERS_PER_THREAD; i++){
                    dummies.add(database.generateUser());
                }

                nextUser = 0;
                nextUserToFollow = 0;
                nextDummy = 0;

                while (flagWarmingUp.get()) { // warm up
                    type = chooseOperation();
                    compute(type);
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
                        compute(type);
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
                                compute(type);
//                                compute(type, timeLocalOperations, timeLocalDurations, false,num);
                                cleanTimeline = num++ % (2 * _nbUserInit) == 0;
//                                num++;
                            }
                        }else{

                            compute(type);
//                            compute(type, timeLocalOperations, timeLocalDurations, false, num);
                            num++;
//
//                            cleanTimeline = num++ % (2 * _nbUserInit) == 0;
                        }
                    }
                }

                endTimeBenchmark = System.nanoTime();
                totalTime.addAndGet(endTimeBenchmark - startTimeBenchmark);

                if (_completionTime){
                    latchFillCompletionTime.countDown();
                    latchFillCompletionTime.await();
                }

            } catch (Throwable e) {
                e.printStackTrace();
                throw new RuntimeException();
            }
            return null;
        }

        public void dummyFunction() throws InterruptedException {
            nbAttempt++;
//            TimeUnit.NANOSECONDS.sleep(1);
//            System.nanoTime();
        }

        public operationType chooseOperation(){
            operationType type;
            int val = random.nextInt(100);
            if (val < ratiosArray[0]){ // add
                if (val%2 == 0){
                    type = ADD;
                }else{
                    type = REMOVE;
                }
            } else if (val >= ratiosArray[0] && val < ratiosArray[0]+ ratiosArray[1]){ //follow or unfollow
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

        public void compute(operationType type) {
            try {
                user = users.get(nextUser++ % MAX_USERS_PER_THREAD);
                switch (type) {
                    case ADD:
                        dummy = dummies.get(nextDummy++ % MAX_DUMMY_USERS_PER_THREAD);
                        database.addOriginalUser(dummy);
                        break;
                    case REMOVE:
                        dummy = dummies.get(nextDummy++ % MAX_DUMMY_USERS_PER_THREAD);
                        database.removeUser(dummy);
                        break;
                    case FOLLOW:
                    case UNFOLLOW:
                        userToFollow = usersToFollow.get(nextUserToFollow++ % MAX_USERS_TO_FOLLOW_PER_THREAD);
                        database.followUser(user, userToFollow);
                        database.unfollowUser(user, userToFollow);
                        break;
                    case TWEET:
                        database.tweet(user, msg);
                        break;
                    case PROFILE:
                        database.updateProfile(user);
                        break;
                    case READ:
                        database.showTimeline(user);
                        break;
                    case GROUP:
                        if (database.getMapCommunityStatus().get(user) == 0) {
                            database.getMapCommunityStatus().put(user, 1);
                            database.joinCommunity(user);
                        } else {
                            database.getMapCommunityStatus().put(user, 0);
                            database.leaveCommunity(user);
                        }
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + type);
                }
            } catch (Throwable e) {
                e.printStackTrace();
                System.exit(-1);
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
                throw new RuntimeException();
            }
            return null;
        }

        private void saveOperationDistribution() throws IOException {
            System.out.println("Save operation duration distribution");

            Map<Long, Integer> map;

            int binSize = 10000;

            PrintWriter printWriter;
            FileWriter fileWriter;

            for (operationType type: operationType.values()){
                fileWriter = new FileWriter("Duration_"+type.toString()+"_Distribution_"+ _tag + "_" + _nbUserInit + "_Users_" + _nbThreads + "_Threads.txt", false);
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
