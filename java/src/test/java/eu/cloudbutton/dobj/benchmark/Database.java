package eu.cloudbutton.dobj.benchmark;

import eu.cloudbutton.dobj.Factory;
import eu.cloudbutton.dobj.utils.FactoryIndice;
import eu.cloudbutton.dobj.Timeline;
import eu.cloudbutton.dobj.key.Key;
import eu.cloudbutton.dobj.key.KeyGenerator;
import eu.cloudbutton.dobj.key.SimpleKeyGenerator;
import eu.cloudbutton.dobj.utils.SegmentAware;
import lombok.Getter;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.math3.distribution.ParetoDistribution;
import org.apache.commons.math3.random.RandomDataGenerator;

@Getter
public class Database {

    private final String typeMap;
    private final String typeSet;
    private final String typeQueue;
    private final String typeCounter;
    private final int nbThread;
    private final int nbUsers;
    private final Map<Key, Set<Key>> mapFollowers;
    private final Map<Key, Set<Key>> mapFollowing;
    private final Map<Key, Timeline<String>> mapTimelines;
    private final KeyGenerator keyGenerator;
    private final ConcurrentSkipListMap<Long, Key> usersFollowProbability;
    private final ThreadLocal<ConcurrentSkipListMap<Long,Key>> localUsersUsageProbability;
    private final ThreadLocal<Long> localUsersUsageProbabilityRange;
    private final List<List<Key>> listLocalUser;
    private final List<Map<Key,Integer>> mapUsersFollowing;
    private final Map<Key, AtomicInteger> mapNbFollowers;
    private final AtomicInteger count;
    private final FactoryIndice factoryIndice;
    private final List<Integer> powerLawArray;
    private long usersFollowProbabilityRange;
    private List<Key> listAllUser;
    private final ThreadLocal<Random> random;
    private List<Double> listNbFollower = new ArrayList<>(Arrays.asList(0.7, 0.5, 0.5 , 0.4, 0.4, 0.2, 0.2, 0.1, 0.1, 0.1));
    private List<Double> listNbFollowing = new ArrayList<>(Arrays.asList(0.5, 0.4, 0.4 , 0.3, 0.3, 0.1, 0.1, 0.05, 0.05, 0.05));
    ThreadLocal<Integer> threadID;
    private static final double SCALE = 1.0; // Paramètre d'échelle de la loi de puissance
    private static final double SHAPE = 1.0; // Paramètre de forme de la loi de puissance


    public Database(String typeMap, String typeSet, String typeQueue, String typeCounter,
                    int nbThread, int nbUserInit, int nbUserMax,
                    List<Integer> powerLawArray) throws ClassNotFoundException{

        this.typeMap = typeMap;
        this.typeSet = typeSet;
        this.typeQueue = typeQueue;
        this.typeCounter = typeCounter;
        this.nbThread = nbThread;
        this.factoryIndice = new FactoryIndice(nbThread);
        this.random = ThreadLocal.withInitial(() -> new Random(94));

        if (typeMap.contains("Extended")){
            mapFollowers = Factory.createMap(typeMap, factoryIndice);
            mapFollowing = Factory.createMap(typeMap, factoryIndice);
            mapTimelines = Factory.createMap(typeMap, factoryIndice);
        }else{
            mapFollowers = Factory.createMap(typeMap, nbThread);
            mapFollowing = Factory.createMap(typeMap, nbThread);
            mapTimelines = Factory.createMap(typeMap, nbThread);
        }

        usersFollowProbability = new ConcurrentSkipListMap<>();
        localUsersUsageProbability = ThreadLocal.withInitial(ConcurrentSkipListMap::new);
        localUsersUsageProbabilityRange = new ThreadLocal<>();
        nbUsers = nbUserInit;
        keyGenerator = new SimpleKeyGenerator(nbUserMax);
        listLocalUser = new ArrayList<>();
        mapUsersFollowing = new ArrayList<>();
        count = new AtomicInteger();
        this.powerLawArray = powerLawArray;
        listAllUser = new ArrayList<>();
        mapNbFollowers = new ConcurrentHashMap<>();
        threadID = new ThreadLocal<>();


        for (int i = 0; i < nbThread; i++) {
            listLocalUser.add(new ArrayList<>());
            mapUsersFollowing.add(new ConcurrentSkipListMap<>());
        }

        System.out.println("generate user");
        generateUsers();
    }

    public void fill(CountDownLatch latchAddUser, CountDownLatch latchHistogram,  Map<Key, Queue<Key>> localUsersFollow) throws InterruptedException, ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException, OutOfMemoryError {
//        System.out.println("start adding user phase thread : " + Thread.currentThread().getName());

        threadID.set(count.getAndIncrement());
        List<Key> users = listLocalUser.get(threadID.get());

        //adding all users

        long somme = 0;
        for (Key user : users) {
//            somme += powerLawArray.get(random.get().nextInt(powerLawArray.size()));
            somme += 1; // Each user have the same probability to be chosen
            addOriginalUser(user);
            localUsersUsageProbability.get().put(somme, user);
            localUsersFollow.put(user, new LinkedList<>());
        }

        localUsersUsageProbabilityRange.set(somme);

        System.out.println("Donne adding users");

        latchAddUser.countDown();
        latchAddUser.await();


//        followingTest(threadID);
        followingPhase(threadID.get(), localUsersFollow);

        latchHistogram.countDown();

    }

    public Key generateUser(){
        return keyGenerator.nextKey();
    }

    public void generateUsers(){

        Set<Key> localSetUser = new HashSet<>();
        long sommeProba = 0;
        int sizeArray = powerLawArray.size();
        int maxFollowing, maxFollower, nbFollowing, nbFollower;

	    double outRatio = 16000000 / 175000000.0;
        maxFollowing = (int) (nbUsers*outRatio);
        double inRatio = 20000000 / 175000000.0;
        maxFollower = (int) (nbUsers * inRatio);




        for (int i = 0; i < nbUsers;) {
//            System.out.println(i);
            Key user = generateUser();
            if (localSetUser.add(user)){
                /*int powerLawVal = powerLawArray.get(random.get().nextInt(sizeArray));

                if (nbUsers >= 100){
                    nbFollowing = Math.min(2*powerLawArray.get(random.get().nextInt(sizeArray)), maxFollowing);
                    nbFollower =  Math.min(2*powerLawArray.get(random.get().nextInt(sizeArray)), maxFollower);

                    while (nbFollower <= 0) {
                        nbFollower *= 10;
                        System.out.println("nbFollower " + nbFollower);
                    }

                    while (nbFollowing <= 0) {
                        nbFollowing *= 10;
                        System.out.println(nbFollowing);
                    }

                }else{
//                    nbFollowing = (int) (listNbFollowing.get(random.get().nextInt(listNbFollowing.size())) * nbUsers);
//                    nbFollower = (int) (listNbFollower.get(random.get().nextInt(listNbFollowing.size())) * nbUsers);
                    nbFollowing = (int) (listNbFollowing.get(i%listNbFollowing.size()) * nbUsers);
                    nbFollower = (int) (listNbFollower.get(i%listNbFollower.size()) * nbUsers);
//                    nbFollowing = 10;
//                    nbFollower = 20;
                }*/

                nbFollower = generatePowerLawValue(nbUsers);
                nbFollowing = generatePowerLawValue(nbUsers);
//                sommeProba += powerLawVal;
                sommeProba += 1;

                usersFollowProbability.put(sommeProba, user);
                listLocalUser.get(i%nbThread).add(user);
                mapUsersFollowing.get(i%nbThread).put(user, nbFollowing);
                mapNbFollowers.put(user, new AtomicInteger(nbFollower));
                i++;
            }
        }

//        System.out.println(mapUsersFollowing);

        listAllUser.addAll(localSetUser);
        System.out.println("Done generating users");

        usersFollowProbabilityRange = sommeProba;
    }

    public static int generatePowerLawValue(int maxValue) {
        ParetoDistribution pareto = new ParetoDistribution(SHAPE,SCALE);
        double y = pareto.sample();

        // Appliquer une transformation linéaire pour mettre à l'échelle la valeur
        double scaledValue = (y / SCALE) * maxValue;

        System.out.println(scaledValue);

        return (int) scaledValue;
    }

    public void followingTest(int threadID) throws InterruptedException { // Each user follow only one user at first
        List<Key> users = listLocalUser.get(threadID);
        ThreadLocal<Random> random = ThreadLocal.withInitial(Random::new);

        for (Key userA: users){
            long val = random.get().nextLong()%usersFollowProbabilityRange;
            Key userB = usersFollowProbability.ceilingEntry(val).getValue();
            followUser(userA, userB);
        }
    }

    public void followingPhase(int threadID, Map<Key, Queue<Key>> localUsersFollow) throws InterruptedException {
        System.out.println("start following phase thread : " + Thread.currentThread().getName());

        List<Key> users = listLocalUser.get(threadID);

        int j = 0;
        for (Key userA: users){
            if(++j%100000 == 0)
                System.out.println(j);

            Queue<Key> usersFollow = localUsersFollow.get(userA);
            int nbFollow = mapUsersFollowing.get(threadID).get(userA);
//            System.out.println(nbFollow);
//	        System.out.println(nbFollow);

            int nbFailFollow = 0;
            for (int i = 0; i < nbFollow;) {


//                System.out.println(i + " | " + nbFollow);

//                randVal = random.get().nextLong() % usersFollowProbabilityRange;
//                Key userB = usersFollowProbability.ceilingEntry(randVal).getValue();

                Key userB;
                userB = listAllUser.get(i%nbUsers);

                assert userB != null : "User generated is null";

                if (mapNbFollowers.get(userB).getAndDecrement() > 0) {
                    followUser(userA, userB);
		            usersFollow.add(userB);
                    i++;
                }else
                    nbFailFollow++;

                if (nbFailFollow >= nbUsers)
                    break;
            }

        }
        System.out.println("end following phase thread : " + Thread.currentThread().getName());
    }

    public String computeHistogram(int range, int max, String type){

//        NavigableMap<Integer,Integer> mapHistogram = new TreeMap<>();
        Map<Key, Set<Key>> computedMap = null;

        if (type.equals("Follower")) {
            computedMap = mapFollowers;
        }else if (type.equals("Following")){
            computedMap = mapFollowing;
        }

//        for (int i = 0; i <= max; i+=max/range) {
//            mapHistogram.put(i,0);
//        }

        int v,k;

        assert computedMap != null : "Failed initialize map while computing histogram";

        String values = "";
        for (Set<Key> s : computedMap.values()) {
            values += s.size() +" ";
//            k = mapHistogram.ceilingKey(s.size());
//            v = mapHistogram.get(k) + 1;
//            mapHistogram.put(k, v);
        }

//        int totalUser = 0;
//
//        for (int nb : mapHistogram.values())
//            totalUser += nb;
//
//        assert  totalUser == nbUsers : "Wrong number of user in histogram";

        return values;
//        return mapHistogram;
    }

    public void addOriginalUser(Key user) throws ClassNotFoundException {
        mapFollowers.put(user, new ConcurrentSkipListSet<>());
        mapFollowing.put(user, new HashSet<>());

//        if (typeSet.contains("Extended"))
//            mapFollowing.put(user, Factory.createSet(typeSet, factoryIndice));
//        else
//            mapFollowing.put(user, Factory.createSet(typeSet, nbThread));
        mapTimelines.put(user, new Timeline(Factory.createQueue(typeQueue)));
    }

    public void addUser(Key user, Set<Key> dummySet, Timeline<String> dummyTimeline) {
//        assert user != null : "User is null";
//        assert dummySet != null : "Set is null";
//        assert dummyTimeline != null : "Timeline is null";

        mapFollowers.put(user,dummySet);
        mapFollowing.put(user, dummySet);
        mapTimelines.put(user, dummyTimeline);
    }

    public void removeUser(Key user){
        mapFollowers.remove(user);
        mapFollowing.remove(user);
        mapTimelines.remove(user);
    }

    // Adding user_A to the followers of user_B
    // and user_B to the following of user_A
    public void followUser(Key userA, Key userB) throws InterruptedException {


        Set set;

        set = mapFollowers.get(userB);
        set.add(userA);



        set = mapFollowing.get(userA);
        set.add(userB);

//        System.out.println(Thread.currentThread().getName() + " is making " + userA + " follows " + userB + " and it has indice : " + ((SegmentAware)userB).getReference().get());

//        TimeUnit.SECONDS.sleep(100000);

//        if (set.add(userB)){
////            System.out.println(Thread.currentThread().getName() + " add first " + userB + " to the set of following of " + userA);
//        }else{
////            System.out.println(Thread.currentThread().getName() + " add " + userB + " in the wrong set");
//            System.exit(0);
//        }

    }

    // Removing user_A to the followers of user_B
    // and user_B to the following of user_A
    public void unfollowUser(Key userA, Key userB){
        mapFollowers.get(userB).remove(userA);
        mapFollowing.get(userA).remove(userB);
    }

    public void tweet(Key user, String msg) throws InterruptedException {
        Set<Key> set = mapFollowers.get(user);

        for (Key follower : set) {
            Timeline timeline = mapTimelines.get(follower);

            timeline.add(msg);
        }
    }

    public void showTimeline(Key user) throws InterruptedException {
        mapTimelines.get(user).read();
    }

}




