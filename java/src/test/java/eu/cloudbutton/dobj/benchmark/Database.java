package eu.cloudbutton.dobj.benchmark;

import eu.cloudbutton.dobj.Factory;
import eu.cloudbutton.dobj.utils.FactoryIndice;
import eu.cloudbutton.dobj.Timeline;
import eu.cloudbutton.dobj.key.Key;
import eu.cloudbutton.dobj.key.KeyGenerator;
import eu.cloudbutton.dobj.key.SimpleKeyGenerator;
import lombok.Getter;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

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
    private final AtomicInteger count;
    private final FactoryIndice factoryIndice;
    private final List<Integer> powerLawArray;
    private long usersFollowProbabilityRange;
    private List<Double> listNbFollowing = new ArrayList<>(Arrays.asList(0.005, 0.003, 0.0015 , 0.0007, 0.0004, 0.0002, 0.0001, 0.00005, 0.00002, 0.00001));
    private List<Double> listNbFollower = new ArrayList<>(Arrays.asList(0.1, 0.06, 0.03, 0.014, 0.008, 0.004, 0.002, 0.001, 0.0004, 0.0002));
    private List<Key> listAllUser;
    private final ThreadLocal<Random> random;


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


        for (int i = 0; i < nbThread; i++) {
            listLocalUser.add(new ArrayList<>());
            mapUsersFollowing.add(new ConcurrentSkipListMap<>());
        }

        System.out.println("generate user");
        generateUsers();
    }

    public void fill(CountDownLatch latchAddUser, CountDownLatch latchHistogram,  Map<Key, List<Key>> localUsersFollow) throws InterruptedException, ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException, OutOfMemoryError {
//        System.out.println("start adding user phase thread : " + Thread.currentThread().getName());

        int threadID = count.getAndIncrement();
        List<Key> users = listLocalUser.get(threadID);

        //adding all users

        long somme = 0;
        for (Key user : users) {
            somme += 1; // Each user have the same probability to be choose
            addUser(user);
            localUsersUsageProbability.get().put(somme, user);
            localUsersFollow.put(user, new ArrayList<>());
        }

        localUsersUsageProbabilityRange.set(somme);

        System.out.println("Donne adding users");

        latchAddUser.countDown();
        latchAddUser.await();


//        followingTest(threadID);
        followingPhase(threadID, localUsersFollow);

        latchHistogram.countDown();

    }

    public Key generateUser(){
        return keyGenerator.nextKey();
    }

    public void generateUsers(){

        Set<Key> localSetUser = new HashSet<>();
        long sommeProba = 0;
        int sizeArray = powerLawArray.size();
        int maxFollowing;

	    double outRatio = 40000 / 175000000.0;
        maxFollowing = (int) (nbUsers*outRatio);

        for (int i = 0; i < nbUsers;) {
            Key user = generateUser();
            if (localSetUser.add(user)){
                int powerLawVal = powerLawArray.get(random.get().nextInt(sizeArray)),
                        nbFollowing = Math.min(powerLawArray.get(random.get().nextInt(sizeArray)), maxFollowing);

                sommeProba += powerLawVal;

                usersFollowProbability.put(sommeProba, user);
                listLocalUser.get(i%nbThread).add(user);
                mapUsersFollowing.get(i%nbThread).put(user, nbFollowing);

                i++;
            }
        }

//        System.out.println(mapUsersFollowing);

        listAllUser.addAll(localSetUser);
        System.out.println("Done generating users");

        usersFollowProbabilityRange = sommeProba;
    }

    public void followingTest(int threadID){ // Each user follow only one user at first
        List<Key> users = listLocalUser.get(threadID);
        ThreadLocal<Random> random = ThreadLocal.withInitial(Random::new);

        for (Key userA: users){
            long val = random.get().nextLong()%usersFollowProbabilityRange;
            Key userB = usersFollowProbability.ceilingEntry(val).getValue();
            followUser(userA, userB);
        }
    }

    public void followingPhase(int threadID, Map<Key, List<Key>> localUsersFollow){
        System.out.println("start following phase thread : " + Thread.currentThread().getName());

        List<Key> users = listLocalUser.get(threadID);

	    double inRatio = 50000 / 175000000.0;
        int maxFollower = (int) (nbUsers * inRatio);

        System.out.println("MAX FOLLOWER : " + maxFollower);

        int j = 0;
        for (Key userA: users){

            if(++j%100000 == 0)
                System.out.println(j);

            List<Key> usersFollow = localUsersFollow.get(userA);
            int nbFollow = mapUsersFollowing.get(threadID).get(userA);
//	        System.out.println(nbFollow);
            for (int i = 0; i < nbFollow;) {

//                randVal = random.get().nextLong() % usersFollowProbabilityRange;
//                Key userB = usersFollowProbability.ceilingEntry(randVal).getValue();

                Key userB;
                userB = listAllUser.get(random.get().nextInt(nbUsers));

                assert userB != null : "User generated is null";

                if (mapFollowers.get(userB).size() <= maxFollower) {
                    followUser(userA, userB);
		            usersFollow.add(userB);
                    i++;
                }
            }

        }
        System.out.println("end following phase thread : " + Thread.currentThread().getName());
    }

    public Map<Integer,Integer> computeHistogram(int range, int max, String type){

        NavigableMap<Integer,Integer> mapHistogram = new TreeMap<>();
        Map<Key, Set<Key>> computedMap = null;

        if (type.equals("Follower")) {
            computedMap = mapFollowers;
        }else if (type.equals("Following")){
            computedMap = mapFollowing;
        }

        for (int i = 0; i <= max; i+=max/range) {
            mapHistogram.put(i,0);
        }

        int v,k;

        assert computedMap != null : "Failed initialize map while computing histogram";

        for (Set<Key> s : computedMap.values()) {
            k = mapHistogram.ceilingKey(s.size());
            v = mapHistogram.get(k) + 1;
            mapHistogram.put(k, v);
        }

        int totalUser = 0;

        for (int nb : mapHistogram.values())
            totalUser += nb;

        assert  totalUser == nbUsers : "Wrong number of user in histogram";

        return mapHistogram;
    }

    public void addUser(Key user) throws ClassNotFoundException {
        mapFollowers.put(user, new ConcurrentSkipListSet<>());
        if (typeSet.contains("Extended"))
            mapFollowing.put(user, Factory.createSet(typeSet, factoryIndice));
        else
            mapFollowing.put(user, Factory.createSet(typeSet, nbThread));
        mapTimelines.put(user, new Timeline(Factory.createQueue(typeQueue)));
    }

    // Adding user_A to the followers of user_B
    // and user_B to the following of user_A
    public void followUser(Key userA, Key userB){
        Set set;

        set = mapFollowers.get(userB);
        set.add(userA);

        set = mapFollowing.get(userA);
        set.add(userB);
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




