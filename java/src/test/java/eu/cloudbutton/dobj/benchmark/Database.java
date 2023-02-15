package eu.cloudbutton.dobj.benchmark;

import eu.cloudbutton.dobj.Factory;
import eu.cloudbutton.dobj.Timeline;
import eu.cloudbutton.dobj.asymmetric.swmr.SWMRHashMap;
import eu.cloudbutton.dobj.key.Key;
import eu.cloudbutton.dobj.key.KeyGenerator;
import eu.cloudbutton.dobj.key.SimpleKeyGenerator;
import eu.cloudbutton.dobj.segmented.ExtendedSegmentedHashMap;
import eu.cloudbutton.dobj.segmented.SegmentedHashMap;
import lombok.Getter;
import nl.peterbloem.powerlaws.DiscreteApproximate;

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
    private final double alpha;
    private final int nbThread;
    private final int nbUsers;
    private final Map<Key, Set<Key>> mapFollowers;
    private final Map<Key, Set<Key>> mapFollowing;
    private final Map<Key, Timeline<String>> mapTimelines;
    private ThreadLocalRandom random;
    private KeyGenerator keyGenerator;
    private ConcurrentSkipListMap<Long, Key> usersProbability;
    private ThreadLocal<ConcurrentSkipListMap<Long,Key>> localUsersProbability;
    private Queue<Key> queueUsers;
    private long usersProbabilityRange;
    private ThreadLocal<Long> localUsersProbabilityRange;
    private List<Integer> powerlawArray;
    private List<List<Key>> usersCollections;
    private AtomicInteger count;

    public Database(String typeMap, String typeSet, String typeQueue, String typeCounter, double alpha, int nbThread, int nbUserMax, List<Integer> powerlawArray) throws ClassNotFoundException{

        this.typeMap = typeMap;
        this.typeSet = typeSet;
        this.typeQueue = typeQueue;
        this.typeCounter = typeCounter;
        this.alpha = alpha;
        this.nbThread = nbThread;
        mapFollowers = new ConcurrentHashMap<>();
//        mapFollowers = (ExtendedSegmentedHashMap<Key, Set<Key>>) Factory.createMap(typeMap, nbThread);
//        mapFollowing = (ExtendedSegmentedHashMap<Key, Set<Key>>) Factory.createMap(typeMap, nbThread);
//        mapFollowing = new SegmentedHashMap<>(nbThread);
        mapFollowing = Factory.createMap(typeMap, nbThread);
        mapTimelines = new ConcurrentHashMap<>();
        usersProbability = new ConcurrentSkipListMap<>();
        localUsersProbability = ThreadLocal.withInitial(ConcurrentSkipListMap::new);
        localUsersProbabilityRange = new ThreadLocal<>();
        random = null;
        queueUsers = new ConcurrentLinkedQueue<>();
        this.powerlawArray = powerlawArray;
        nbUsers = powerlawArray.size();
        keyGenerator = new SimpleKeyGenerator(nbUserMax);
        usersCollections = new ArrayList<>();
        count = new AtomicInteger();

        for (int i = 0; i < nbThread; i++) {
            usersCollections.add(new ArrayList<>());
        }

        generateUsers();
    }

    public void fill(CountDownLatch latchDatabase, Map<Key, Queue<Key>> localUsersFollow) throws InterruptedException, ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException, OutOfMemoryError {

        random = ThreadLocalRandom.current();

        long somme = 0;
        Key user, userB = null;
        List<Key> users = usersCollections.get(count.getAndIncrement());

        //adding all users

//        System.out.println("Adding users");

        List<Integer> data = new DiscreteApproximate(1, alpha).generate(users.size());

        int i = 0;
        for (int val: data){
            if (val < 0)
                data.set(i, 1);
            i++;
        }

        Collections.sort(data);
        for (int id = 0; id < users.size(); id++) {

            somme += data.get(id);
            user = users.get(id);
            addUser(user);
            localUsersProbability.get().put(somme, user);
            localUsersFollow.put(user, new LinkedList<>());
        }


        localUsersProbabilityRange.set(somme);

        latchDatabase.countDown();
        latchDatabase.await();


//        System.out.println("following phase");
        //Following phase

        long max = nbUsers;

        i = 0;
        for (int val: data){
            if (val >= max) {
                data.set(i, (int) max);
            }
            i++;
        }


        long randVal;

        for (Key userA: localUsersFollow.keySet()){
            int nbFollow = (int) Math.max(Math.min(powerlawArray.get(random.nextInt(nbUsers)), nbUsers*0.00432), 1); // nbFollow max to match Twitter Graph
            assert nbFollow > 0 : "not following anyone";
            for(int j = 0; j < nbFollow; j++){

                try{
                    randVal = random.nextLong(usersProbabilityRange);
                    userB = usersProbability.ceilingEntry(randVal).getValue();
                    assert userB != null : "User generated is null";

                    followUser(userA, userB);
                }catch (Exception e){
                    e.printStackTrace();
                    System.exit(1);
                }
            }
            Set set = mapFollowers.get(userB);
            assert set.size() > 0 : userB + " from " + Thread.currentThread().getName() + " do not follow anyone.";
        }

    }

    public Key generateUser(){
        return keyGenerator.nextKey();
    }

    public void generateUsers(){
        int i = 0;
        long somme = 0;
        Set<Key> localSetUser = new HashSet<>();
        Collections.sort(powerlawArray);

        while (localSetUser.size() < powerlawArray.size()){
            Key user = generateUser();
            if (localSetUser.add(user)){
                usersCollections.get(i%nbThread).add(user);
                somme += this.powerlawArray.get(i);
                usersProbability.put(somme, user);
                i++;
            }
        }

        usersProbabilityRange = somme;
    }

    public void addUser(Key user) throws ClassNotFoundException {
        mapFollowers.put(user, new ConcurrentSkipListSet<>());
        mapFollowing.put(user, new HashSet<>());
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
        mapFollowers.get(userB);//.remove(userA);
        mapFollowing.get(userA);//.remove(userB);
    }

    public void tweet(Key user, String msg) throws InterruptedException {
        Set<Key> set = mapFollowers.get(user);

        for (Key follower : set) {
            mapTimelines.get(follower).add(msg);
        }
    }

    public void showTimeline(Key user) throws InterruptedException {
        mapTimelines.get(user).read();
    }
    
}




