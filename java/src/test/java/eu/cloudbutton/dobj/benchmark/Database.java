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
    private long usersFollowProbabilityRange;
    private final ThreadLocal<Long> localUsersUsageProbabilityRange;
    private final List<List<Key>> listLocalUser;
    private final List<Map<Key,Integer>> mapUsersFollowing;
    private final Map<Key,AtomicInteger> mapUsersFollower;
    private final AtomicInteger count;
    private final FactoryIndice factoryIndice;
    private final List<Double> proportionInPowerLawArrayFollowers;
    private final List<Double> proportionOutPowerLawArrayFollowers;
    private List<Double> listNbFollowing = new ArrayList<>(Arrays.asList(0.005, 0.003, 0.0015 , 0.0007, 0.0004, 0.0002, 0.0001, 0.00005, 0.00002, 0.00001));
    private List<Double> listNbFollower = new ArrayList<>(Arrays.asList(0.1, 0.06, 0.03, 0.014, 0.008, 0.004, 0.002, 0.001, 0.0004, 0.0002));

    public Database(String typeMap, String typeSet, String typeQueue, String typeCounter,
                    int nbThread, int nbUserInit, int nbUserMax,
                    List<Double> proportionInPowerLawArrayFollowers, List<Double> proportionOutPowerLawArrayFollowers) throws ClassNotFoundException{

        this.typeMap = typeMap;
        this.typeSet = typeSet;
        this.typeQueue = typeQueue;
        this.typeCounter = typeCounter;
        this.nbThread = nbThread;
        this.factoryIndice = new FactoryIndice(nbThread);

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
        mapUsersFollower = new ConcurrentSkipListMap<>();
        count = new AtomicInteger();
        this.proportionInPowerLawArrayFollowers = proportionInPowerLawArrayFollowers;
        this.proportionOutPowerLawArrayFollowers = proportionOutPowerLawArrayFollowers;


        for (int i = 0; i < nbThread; i++) {
            listLocalUser.add(new ArrayList<>());
            mapUsersFollowing.add(new ConcurrentSkipListMap<>());
        }

        generateUsers();
    }

    public void fill(CountDownLatch latchDatabase,  Map<Key, Queue<Key>> localUsersFollow) throws InterruptedException, ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException, OutOfMemoryError {
//        System.out.println("start adding user phase thread : " + Thread.currentThread().getName());

        int threadID = count.getAndIncrement();
        List<Key> users = listLocalUser.get(threadID);

        //adding all users

        long somme = 0;
        for (Key user : users) {
            somme += 1; // Each user have the same probability to be choose
            addUser(user);
            localUsersUsageProbability.get().put(somme, user);
            localUsersFollow.put(user, new LinkedList<>());
        }

        localUsersUsageProbabilityRange.set(somme);

        latchDatabase.countDown();
        latchDatabase.await();


//        followingTest(threadID);
        followingPhase(threadID, localUsersFollow);
    }

    public Key generateUser(){
        return keyGenerator.nextKey();
    }

    public void generateUsers(){

        Set<Key> localSetUser = new HashSet<>();
        long sommeFollower = 0, nbTotalFollower = 0, nbTotalFollowing = 0;
        Random random = new Random();
        int sizeArray = proportionInPowerLawArrayFollowers.size();
        int maxFollower, maxFollowing;

        maxFollower = (int) ((8.4 * nbUsers)/100);
        maxFollowing = (int) ((0.43 * nbUsers)/100);

        for (int i = 0; i < nbUsers;) {
            Key user = generateUser();
            if (localSetUser.add(user)){
                int nbFollower = (int) ((proportionInPowerLawArrayFollowers.get(i%sizeArray) * nbUsers)/100);
                int nbFollowing = (int) ((proportionOutPowerLawArrayFollowers.get(random.nextInt(sizeArray)) * nbUsers)/100);

                sommeFollower += nbFollower;

                usersFollowProbability.put(sommeFollower, user);
                listLocalUser.get(i%nbThread).add(user);
                mapUsersFollowing.get(i%nbThread).put(user, Math.min(nbFollowing, maxFollowing));
                mapUsersFollower.put(user, new AtomicInteger(Math.min(nbFollower, maxFollower)));

                nbTotalFollower += Math.min(nbFollower, maxFollower);
                nbTotalFollowing += Math.min(nbFollowing, maxFollowing);

                i++;
            }

        }

        usersFollowProbabilityRange = sommeFollower;
        adjustDegrees(nbTotalFollower-nbTotalFollowing);
    }

    public void adjustDegrees(long differential){

        if (differential < 0){ // More following than follower
            while (differential < 0){
                for (AtomicInteger count : mapUsersFollower.values()){
                    count.incrementAndGet();
                    differential++;
                    if (differential == 0)
                        return;
                }
            }
        }
        else if (differential > 0){ // More follower than following
            while (differential > 0){
                for (Map<Key, Integer> map : mapUsersFollowing){
                    for (Key user: map.keySet()){
                        map.compute(user, (key,value) -> value + 1);
                        differential--;
                        if (differential == 0)
                            return;
                    }
                }
            }
        }

        int nbFollower = 0, nbFollowing = 0;

        for (AtomicInteger count : mapUsersFollower.values()){
            nbFollower += count.get();
        }

        for (Map<Key, Integer> map : mapUsersFollowing){
            for (int val: map.values())
                nbFollowing += val;
        }

        assert nbFollower == nbFollowing : "degrees In and degrees Out are different";

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

    public void followingPhase(int threadID, Map<Key, Queue<Key>> localUsersFollow){
        System.out.println("start following phase thread : " + Thread.currentThread().getName());

        List<Key> users = listLocalUser.get(threadID);

        for (Key userA: users){

            int nbFollow = mapUsersFollowing.get(threadID).get(userA);
//            nbFollow = nbFollow > 0 ? nbFollow : 1;

            for (int i = 0; i < nbFollow; i++) {
                for (Key userB: mapUsersFollower.keySet()) {
                    if(!localUsersFollow.get(userA).contains(userB)){

                        int nbFollowerLeft = mapUsersFollower.get(userB).getAndDecrement();

                        if (nbFollowerLeft > 0){
                            followUser(userA, userB);
                            localUsersFollow.get(userA).add(userB);
                        }
                    }
                }
            }

//            Set set = mapFollowers.get(userB);
//            assert set.size() > 0 : userB + " from " + Thread.currentThread().getName() + " do not follow anyone.";
        }


        System.out.println("end following phase thread : " + Thread.currentThread().getName());


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




