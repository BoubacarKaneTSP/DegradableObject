package eu.cloudbutton.dobj.benchmark;

import eu.cloudbutton.dobj.Factory;
import eu.cloudbutton.dobj.Timeline;
import eu.cloudbutton.dobj.key.Key;
import eu.cloudbutton.dobj.key.KeyGenerator;
import eu.cloudbutton.dobj.key.RetwisKeyGenerator;
import eu.cloudbutton.dobj.key.SimpleKeyGenerator;
import lombok.Getter;
import nl.peterbloem.powerlaws.DiscreteApproximate;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.*;

@Getter
public class Database {

    private final String typeMap;
    private final String typeSet;
    private final String typeQueue;
    private final String typeCounter;
    private final double alpha;
    private final int nbThread;
    private final Map<Key, Set<Key>> mapFollowers;
    private final Map<Key, Set<Key>> mapFollowing;
    private final Map<Key, Timeline<String>> mapTimelines;
    private ThreadLocalRandom random;
    private KeyGenerator keyGenerator;
    private boolean useCollisionKey;
    private ConcurrentSkipListMap<Integer, Key> usersProbability;
    private ThreadLocal<ConcurrentSkipListMap<Integer,Key>> localUsersProbability;
    private Queue<Key> queueUsers;
    private int usersProbabilityRange;
    private ThreadLocal<Integer> localUsersProbabilityRange;

    public Database(String typeMap, String typeSet, String typeQueue, String typeCounter, double alpha, int nbThread, boolean useCollisionKey, int nbUsersInit, int nbUserMax) throws ClassNotFoundException{

        this.typeMap = typeMap;
        this.typeSet = typeSet;
        this.typeQueue = typeQueue;
        this.typeCounter = typeCounter;
        this.alpha = alpha;
        this.nbThread = nbThread;
        mapFollowers = new ConcurrentHashMap<>();
        mapFollowing = Factory.createMap(typeMap, nbThread);
        mapTimelines = new ConcurrentHashMap<>();
        usersProbability = new ConcurrentSkipListMap<>();
        localUsersProbability = ThreadLocal.withInitial(ConcurrentSkipListMap::new);
        localUsersProbabilityRange = new ThreadLocal<>();
        random = null;
        this.useCollisionKey = useCollisionKey;
        this.queueUsers = new ConcurrentLinkedQueue<>();

        List<Integer> data = new DiscreteApproximate(1, alpha).generate(nbUsersInit);
        keyGenerator = new SimpleKeyGenerator(nbUserMax);

        int somme = 0;

//        System.out.println("Adding users");

        for (int i = 0; i < data.size();) {

            Key user = addUser();
            if (!queueUsers.contains(user)) {
                queueUsers.offer(user);
                somme += data.get(i);
                usersProbability.put(somme, user);
                i++;
            }
        }
        usersProbabilityRange = somme;
    }

    public void fill(int nbUsers, CountDownLatch latchDatabase, Map<Key, Queue<Key>> usersFollow) throws InterruptedException, ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException, OutOfMemoryError {

        random = ThreadLocalRandom.current();

        int userPerThread, somme = 0;
        Key user, userB = null;

        //adding all users

//        System.out.println("Adding users");

        userPerThread = nbUsers / nbThread;

        List<Integer> data = new DiscreteApproximate(1, alpha).generate(userPerThread);

        int i = 0;
        for (int val: data){
            if (val < 0)
                data.set(i, 1);
            i++;
        }

        for (int id = 0; id < userPerThread; id++) {
            somme += data.get(id);
            user = queueUsers.poll();

            usersFollow.put(user, new LinkedList<>());

            localUsersProbability.get().put(somme, user);
        }

        localUsersProbabilityRange.set(somme);

        latchDatabase.countDown();
        latchDatabase.await();

        //Following phase

        long max = nbUsers;

        i = 0;
        for (int val: data){
            if (val >= max) {
                data.set(i, (int) max);
            }
            i++;
        }

        i = 0;

        int randVal;
        Map.Entry<Integer, Key> k;

        System.out.println("data : " + data);
        for (Key userA: usersFollow.keySet()){
            int nbFollow = data.get(i);
            for(int j = 0; j < nbFollow; j++){

                randVal = random.nextInt(usersProbabilityRange);
                k = usersProbability.ceilingEntry(randVal);
                userB = k.getValue();

                followUser(userA, userB);
            }
            i++;
        }
    }

    public Key addUser() throws ClassNotFoundException {

        Key userID;

        userID = keyGenerator.nextKey();
        if (!mapFollowers.containsKey(userID)) {
            mapFollowers.put(userID, new ConcurrentSkipListSet<>());
            mapTimelines.put(userID, new Timeline(Factory.createQueue(typeQueue)));
            mapFollowing.put(userID, new HashSet<>());
        }
        return userID;
    }

    // Adding user_A to the followers of user_B
    // and user_B to the following of user_A
    public void followUser(Key userA, Key userB){
        mapFollowers.get(userB).add(userA);
        mapFollowing.get(userA).add(userB);
    }

    // Removing user_A to the followers of user_B
    // and user_B to the following of user_A
    public void unfollowUser(Key userA, Key userB){
        mapFollowers.get(userB).remove(userA);
        mapFollowing.get(userA).remove(userB);
    }

    public void tweet(Key user, String msg) throws InterruptedException {
        for (Key follower : mapFollowers.get(user)) {
            mapTimelines.get(follower).add(msg);
        }
    }

    public void showTimeline(Key user) throws InterruptedException {
        mapTimelines.get(user).read();
    }
    
}
