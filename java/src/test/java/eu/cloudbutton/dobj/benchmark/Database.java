package eu.cloudbutton.dobj.benchmark;

import eu.cloudbutton.dobj.Factory;
import eu.cloudbutton.dobj.key.RetwisKeyGenerator;
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
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.RandomGeneratorFactory;

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
    private long usersFollowProbabilityRange;
    private List<Key> listAllUser;
    private final ThreadLocal<Random> random;
    ThreadLocal<Integer> threadID;
    Map<Integer, List<Integer>> mapUsageDistribution;
    private static final double SCALEUSAGE = 1.0; // Paramètre d'échelle de la loi de puissance
    private static final double SCALEFOLLOW = 1.0; // Paramètre d'échelle de la loi de puissance
    private static final double FOLLOWERSHAPE = 1.35; // Paramètre de forme de la loi de puissance
    private static final double FOLLOWINGSHAPE = 1.28; // Paramètre de forme de la loi de puissance


    public Database(String typeMap, String typeSet, String typeQueue, String typeCounter,
                    int nbThread, int nbUserInit, int nbUserMax) throws ClassNotFoundException, InterruptedException {

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
        keyGenerator = new RetwisKeyGenerator(nbUserMax, nbUserMax,FOLLOWERSHAPE);
//        keyGenerator = new SimpleKeyGenerator(nbUserMax);
        listLocalUser = new ArrayList<>();
        mapUsersFollowing = new ArrayList<>();
        count = new AtomicInteger();
        listAllUser = new ArrayList<>();
        mapNbFollowers = new ConcurrentHashMap<>();
        threadID = new ThreadLocal<>();

        List<Integer> powerLawArray = generateValues(nbUsers, nbUserMax, 1, SCALEUSAGE);

//        Collections.sort(powerLawArray);


        mapUsageDistribution = new ConcurrentHashMap<>();

        for (int i = 0; i < nbThread; i++) {
            mapUsageDistribution.put(i, new ArrayList<>());
        }

//        System.out.println(powerLawArray);

//        TimeUnit.SECONDS.sleep(5);
        for (int i = 0; i < nbUsers; i++) {
            mapUsageDistribution.get(i%nbThread).add(powerLawArray.get(i));
        }

        for (int i = 0; i < nbThread; i++) {
            listLocalUser.add(new ArrayList<>());
            mapUsersFollowing.add(new ConcurrentSkipListMap<>());
        }

//        System.out.println(mapUsageDistribution);

        System.out.println("generate user");
        generateUsers();
    }

    public void fill(CountDownLatch latchAddUser, CountDownLatch latchHistogram,  Map<Key, Queue<Key>> localUsersFollow) throws InterruptedException, ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException, OutOfMemoryError {
        System.out.println("start adding user phase thread : " + Thread.currentThread().getName());

        threadID.set(count.getAndIncrement());
        List<Key> users = listLocalUser.get(threadID.get());

        //adding all users
        List<Integer> powerLawArray = mapUsageDistribution.get(threadID.get());

        int powerLawArraySize = powerLawArray.size();

//        System.out.println(powerLawArray);
        long somme = 0;
        int g = 0;
        for (Key user : users) {
//            if (++g%nbUsers*0.05 == 0)
//                System.out.println(g);

            somme += powerLawArray.get(g++%powerLawArraySize)+1;
//            somme += 1; // Each user have the same probability to be chosen
            addOriginalUser(user);
            localUsersUsageProbability.get().put(somme, user);
            localUsersFollow.put(user, new LinkedList<>());
        }
//        System.out.println(localUsersUsageProbability.get().keySet());

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

    public void generateUsers() throws InterruptedException {

        Set<Key> localSetUser = new TreeSet<>();
        long sommeProba = 0;
        int nbFollowing, nbFollower;
        double maxFollower, maxFollowing;
        List<Integer> powerLawArray = generateValues(nbUsers, nbUsers, 1.3, SCALEUSAGE);

        if ((nbUsers*0.43)/100 <= 1)
            maxFollowing = 1;
        else
            maxFollowing = (nbUsers*0.43)/100;

        if ((nbUsers*8.4)/100 <= 1)
            maxFollower = 1;
        else
            maxFollower = (nbUsers*8.4)/100;

        List<Integer> listNbFollowing = generateValues(nbUsers, maxFollowing, FOLLOWINGSHAPE, SCALEFOLLOW);
//        System.out.println(listNbFollowing);
//        System.out.println("listNbFollowing");
        List<Integer> listNbFollower = generateValues(nbUsers, maxFollower, FOLLOWERSHAPE, SCALEFOLLOW);

//        System.out.println(listNbFollower);
//        System.out.println("listNbFollower");

        for (int i = 0; i < nbUsers;) {
            if(i%nbUsers*0.05 == 0)
                System.out.println(i);

            Key user = generateUser();
            if (localSetUser.add(user)){
                nbFollower = Math.max(1,listNbFollower.get(i));
//                nbFollowing =1;
                nbFollowing = Math.max(1,listNbFollowing.get(i));
//                System.out.println("Follower : "+ nbFollower + " | Following : " + nbFollowing);

                sommeProba += powerLawArray.get(i);
//                sommeProba += 1;

                usersFollowProbability.put(sommeProba, user);
                listLocalUser.get(i%nbThread).add(user);
                mapUsersFollowing.get(i%nbThread).put(user, nbFollowing);
                mapNbFollowers.put(user, new AtomicInteger(nbFollower));
                i++; 
            }
	    
        }

//        System.out.println(mapUsersFollowing.get(0).values());

        listAllUser.addAll(localSetUser);
        System.out.println("Done generating users");

        usersFollowProbabilityRange = sommeProba;
//        System.out.println(usersFollowProbabilityRange);
    }

    public static List<Integer> generateValues(int numValues, double desiredMaxValue, double SHAPE, double SCALE) throws InterruptedException {
        List<Double> doubleValues = new ArrayList<>();
        List<Integer> values = new ArrayList<>();

        RandomGenerator rand = RandomGeneratorFactory.createRandomGenerator(new Random(94));
        ParetoDistribution distribution = new ParetoDistribution(rand,SCALE,SHAPE);

        double maxGeneratedValue = 0;
        for (int i = 0; i < numValues; i++) {
            double randomValue = distribution.sample();
            doubleValues.add(randomValue);
            if (randomValue > maxGeneratedValue) {
                maxGeneratedValue = randomValue;
            }
        }

        double scaleFactor = desiredMaxValue / maxGeneratedValue;

        for (int i = 0; i < numValues; i++) {
            double scaledValue = doubleValues.get(i) * scaleFactor;
//            values.add((int) Math.round(doubleValues.get(i))+1);
            values.add((int) Math.round(scaledValue)+1);
//            System.out.println((int) Math.round(doubleValues.get(i))+1);
        }

//        Collections.sort(values);
//        System.out.println(values);
//        System.out.println("aaa");
//        TimeUnit.SECONDS.sleep(5);
//        System.out.println(Collections.max(values));
        return values;
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
        int nbLocalUser = users.size();
        int j = 0;
        long randVal;

        for (Key userA: users){
            if(++j%10000 == 0)
                System.out.println(j);

            Queue<Key> usersFollow = localUsersFollow.get(userA);
            int nbFollow = Math.min(mapUsersFollowing.get(threadID).get(userA), nbLocalUser);
//	        System.out.println(nbFollow);

            int nbFailFollow = 0;

//            for (Long v : usersFollowProbability.keySet())
//                System.out.println(v + " => " + usersFollowProbability.get(v));
//            System.out.println(usersFollowProbability);
            for (long i = 0; i < nbFollow;) {


//                System.out.println(i + " | " + nbFollow);

                randVal = Math.abs(random.get().nextLong() % usersFollowProbabilityRange);
                Key userB =  usersFollowProbability.ceilingEntry(randVal).getValue();

//                randVal = random.get().nextInt(nbLocalUser);
//                Key userB = users.get((int) randVal);

                assert userB != null : "User generated is null";

                if (!usersFollow.contains(userB) && mapNbFollowers.get(userB).getAndDecrement() > 0) {
                    followUser(userA, userB);
                    usersFollow.add(userB);
                    i++;
                }
//                if (mapNbFollowers.get(userB).getAndDecrement() > 0) {
//                }else
//                    nbFailFollow++;
//
//                if (nbFailFollow >= nbUsers)
//                    break;
            }

        }

//        System.out.println();
//        for (Key user: users){
//            if (mapFollowers.get(user).size() > 500) {
//                System.out.println(user + " size followers => " + mapFollowers.get(user).size());
//                System.out.println();
//            }

//            if (mapFollowing.get(user).size() > 500) {
//                System.out.println(user + "size following = >" + mapFollowing.get(user).size());
//                System.out.println();
//            }


//        }

//        TimeUnit.SECONDS.sleep(5);
        System.out.println("end following phase thread : " + Thread.currentThread().getName());
    }

    public String computeHistogram(int range, int max, String type){

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
    public Map computeFollowHistogram(int range, int max, String type){

//        NavigableMap<Integer,Integer> mapHistogram = new TreeMap<>();
        Map<Key, Set<Key>> computedMap = null;
        Map<Integer, Integer> map = new HashMap<>();

        if (type.equals("Follower")) {
            computedMap = mapFollowers;
        }else if (type.equals("Following")){
            computedMap = mapFollowing;
        }


        for (Key key : computedMap.keySet()){
//            System.out.println(key);
            int size = computedMap.get(key).size();
//            if (size > 1000)
//                System.out.println(size);
            if (!map.containsKey(size))
                map.put(size, 1);
            else
                map.put(size, map.get(size) + 1);
        }

        return map;
    }

    public void addOriginalUser(Key user) throws ClassNotFoundException {
//        mapFollowers.put(user, new ConcurrentSkipListSet<>());
//        mapFollowers.put(user, new HashSet<>());
        mapFollowing.put(user, new HashSet<>());

        if (typeSet.contains("Extended"))
            mapFollowers.put(user, Factory.createSet(typeSet, factoryIndice));
        else
            mapFollowers.put(user, Factory.createSet(typeSet, nbThread));
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

        mapFollowers.get(userB)
                .add(userA);
        mapFollowing.get(userA)
                .add(userB);

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
        mapFollowers.get(userB)
                .remove(userA);
        mapFollowing.get(userA)
                .remove(userB);
    }

    public void tweet(Key user, String msg) throws InterruptedException {
        Set<Key> set = mapFollowers.get(user);

//        System.out.println(set.size());
        int i = 0;
        for (Key follower : set) {
            Timeline timeline = mapTimelines.get(follower);
//
//            timeline.add(msg);
//            if (++i >= 1000)
//                break;
        }
    }

    public void showTimeline(Key user) throws InterruptedException {
        mapTimelines.get(user).read();
    }

}




