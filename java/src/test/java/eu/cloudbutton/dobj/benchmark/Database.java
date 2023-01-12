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
import java.util.concurrent.atomic.AtomicLong;

@Getter
public class Database {

    private final String typeMap;
    private final String typeSet;
    private final String typeQueue;
    private final String typeCounter;
    private final Factory factory;
    private final double alpha;
    private final int nbThread;
    private final Map<Key, Set<Key>> mapFollowers;
    private final Map<Key, Set<Key>> mapFollowing;
    private final Map<Key, Timeline<String>> mapTimelines;
    private final AtomicLong next_user_ID;
    private final ThreadLocal<String> threadName;
    private final List<Key> usersProbability;
    private final List<Key> originalUsers;
    private ThreadLocal<List<Key>> localUsersProbability;
    private ThreadLocal<List<Key>> localUsers;
    private ThreadLocalRandom random;
    private final int max_item_per_thread;
    private KeyGenerator keyGenerator;
    private boolean useCollisionKey;

    public Database(String typeMap, String typeSet, String typeQueue, String typeCounter, double alpha, int nbThread, boolean useCollisionKey, int max_item_per_thread) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        this.factory = new Factory();
        Class cls;

        try{
            cls = Class.forName("eu.cloudbutton.dobj.incrementonly."+typeCounter);
        }catch (ClassNotFoundException e){
            if (typeCounter.contains("Sharded"))
                cls = Class.forName("eu.cloudbutton.dobj.sharded."+typeCounter);
            else
                cls = Class.forName("java.util.concurrent."+typeCounter);
        }

        factory.setFactoryCounter(cls);

        try{
            cls = Class.forName("eu.cloudbutton.dobj.mcwmcr."+typeSet);
        }catch (ClassNotFoundException e){
            if (typeSet.contains("Sharded"))
                cls = Class.forName("eu.cloudbutton.dobj.sharded."+typeSet);
            else
                cls = Class.forName("java.util.concurrent."+typeSet);
        }

        factory.setFactorySet(cls);

        try{
            cls = Class.forName("eu.cloudbutton.dobj.asymmetric."+typeQueue);
        }catch (ClassNotFoundException e){
            if (typeQueue.contains("Sharded"))
                cls = Class.forName("eu.cloudbutton.dobj.sharded."+typeQueue);
            else
                cls = Class.forName("java.util.concurrent."+typeQueue);
        }

        factory.setFactoryQueue(cls);

        try{
            cls = Class.forName("eu.cloudbutton.dobj.mcwmcr."+typeMap);
        }catch (ClassNotFoundException e){
            if (typeMap.contains("Sharded"))
                cls = Class.forName("eu.cloudbutton.dobj.sharded."+typeMap);
            else
                cls = Class.forName("java.util.concurrent."+typeMap);
        }

        factory.setFactoryMap(cls);

        this.typeMap = typeMap;
        this.typeSet = typeSet;
        this.typeQueue = typeQueue;
        this.typeCounter = typeCounter;
        this.alpha = alpha;
        this.nbThread = nbThread;
        mapFollowers = new ConcurrentHashMap<>();
        mapFollowing = factory.getMap();
        mapTimelines = new ConcurrentHashMap<>();
        threadName = ThreadLocal.withInitial(() -> Thread.currentThread().getName());
        usersProbability = new CopyOnWriteArrayList<>();
        originalUsers = new CopyOnWriteArrayList<>();
        localUsersProbability = ThreadLocal.withInitial(() -> new ArrayList<>());
        localUsers = ThreadLocal.withInitial(() -> new ArrayList<>());
        random = null;
        next_user_ID = new AtomicLong();
        this.max_item_per_thread = max_item_per_thread;
        this.useCollisionKey = useCollisionKey;

    }

    public void fill(int nbUsers, CountDownLatch latchDatabase, Map<Key, Queue<Key>> usersFollow) throws InterruptedException, ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException {

        random = ThreadLocalRandom.current();
        keyGenerator = useCollisionKey ? new RetwisKeyGenerator(max_item_per_thread, nbUsers, alpha) : new SimpleKeyGenerator(max_item_per_thread);

        int n, userPerThread;
        Key user, userB = null;

        //adding all users

//        System.out.println("Adding users");

//        userPerThread = 1;
        userPerThread = nbUsers / nbThread;

        for (int id = 0; id < userPerThread; id++) {
            user = addUser();

            usersFollow.put(user, new LinkedList<>());

            localUsers.get().add(user);

            try{

                for (int j = 0; j <= user.hashCode(); j++) { // each user have an ID inferior to bound
                    localUsersProbability.get().add(user);
                }
            }catch (OutOfMemoryError e){
                System.out.println("user hash code : " + user.hashCode());
                System.out.println();
                System.out.println(e.getStackTrace());
                System.exit(0);
            }

        }

//        usersProbability.addAll(localUsersProbability.get());
        originalUsers.addAll(localUsers.get());
        latchDatabase.countDown();
        latchDatabase.await();

//        System.out.println("Users added, now " + Thread.currentThread().getName() + " is starting to do the following phase");

        //Following phase
        String msg = "msg";

/*        for (Key userA: usersFollow.keySet()){

            for(int j = 0; j < userA.hashCode(); j++){
//                n = random.nextInt(localUsersProbability.get().size());
                n = random.nextInt(usersProbability.size());

                try{
//                    userB = localUsersProbability.get().get(n);
                    userB = usersProbability.get(n);
                }catch (NullPointerException e){
                    System.exit(0);
                }

//                addUser();
//                showTimeline(userA);
//                tweet(userA, msg);
                followUser(userA, userB);
//                unfollowUser(userA, userB);
//                usersFollow.get(userA).add(userB);
            }
        }*/
    }

    public Key addUser() throws InvocationTargetException, InstantiationException, IllegalAccessException {

        Key userID = keyGenerator.nextKey();

        mapFollowers.put(userID, new ConcurrentSkipListSet<>());
        mapTimelines.put(userID, new Timeline(factory.getQueue()));
        mapFollowing.put(userID, new HashSet<>());

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
