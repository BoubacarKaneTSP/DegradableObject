package eu.cloudbutton.dobj.benchmark;

import eu.cloudbutton.dobj.Factory;
import eu.cloudbutton.dobj.Timeline;
import eu.cloudbutton.dobj.incrementonly.Counter;
import eu.cloudbutton.dobj.incrementonly.FuzzyCounter;
import lombok.Getter;
import nl.peterbloem.powerlaws.DiscreteApproximate;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

@Getter
public class Database {

    private final String typeMap;
    private final String typeSet;
    private final String typeQueue;
    private final String typeCounter;
    private final Factory factory;
    private final double alpha;
    private final int nbThread;
    private final ThreadLocalRandom random;
    private final Map<Long, Set<Long>> mapFollowers;
    private final Map<Long, Set<Long>> mapFollowing;
    private final Map<Long, Timeline<String>> mapTimelines;
    private final Counter next_user_ID;
    private final ThreadLocal<String> threadName;
    private final List<Long> usersProbability;

    public Database(String typeMap, String typeSet, String typeQueue, String typeCounter, double alpha, int nbThread) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        this.factory = new Factory();
        Class cls;

        try{
            cls = Class.forName("eu.cloudbutton.dobj.incrementonly."+typeCounter);
        }catch (ClassNotFoundException e){
            cls = Class.forName("java.util.concurrent."+typeCounter);
        }

        factory.setFactoryCounter(cls);

        try{
            cls = Class.forName("eu.cloudbutton.dobj.mcwmcr."+typeSet);
        }catch (ClassNotFoundException e){
            cls = Class.forName("java.util.concurrent."+typeSet);
        }

        factory.setFactorySet(cls);

        try{
            cls = Class.forName("eu.cloudbutton.dobj.asymmetric."+typeQueue);
        }catch (ClassNotFoundException e){
            cls = Class.forName("java.util.concurrent."+typeQueue);
        }

        factory.setFactoryQueue(cls);

        try{
            cls = Class.forName("eu.cloudbutton.dobj.mcwmcr."+typeMap);
        }catch (ClassNotFoundException e){
            cls = Class.forName("java.util.concurrent."+typeMap);
        }

        factory.setFactoryMap(cls);

        this.typeMap = typeMap;
        this.typeSet = typeSet;
        this.typeQueue = typeQueue;
        this.typeCounter = typeCounter;
        this.alpha = alpha;
        this.nbThread = nbThread;
        this.random = ThreadLocalRandom.current();
        mapFollowers = new ConcurrentHashMap<>();
        mapFollowing = factory.getMap();
        mapTimelines = factory.getMap();
        next_user_ID = factory.getCounter();
        threadName = ThreadLocal.withInitial(() -> Thread.currentThread().getName());
        usersProbability = new CopyOnWriteArrayList<>();

        if (next_user_ID instanceof FuzzyCounter)
            ((FuzzyCounter) next_user_ID).setN(nbThread);
    }

    public void fill(int nbUsers, CountDownLatch latchDatabase, ThreadLocal<Map<Long, Queue<Long>>> usersFollow) throws InterruptedException, ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException {
        int n, userPerThread;
        long user, userB;

        int bound = 1000;

        List<Integer> data = new DiscreteApproximate(1, alpha).generate(bound);
        int i = 0;

        double ratio = 100000 / 175000000.0; //10⁵ is ~ the number of follow max on twitter and 175_000_000 is the number of user on twitter (stats from the article)
        long max = (long) ((long) nbUsers * ratio);

        for (int val: data){
            if (val >= max) {
                data.set(i, (int) max);
            }
            if (val < 1)
                data.set(i, 1);
            i++;
        }

        //adding users

//        System.out.println("Adding users");

        List<Long> localUsers = new ArrayList<>();
        userPerThread = nbUsers / nbThread;

//        System.out.println("userPerThread : " + userPerThread);

        for (int id = 0; id < userPerThread; id++) {
            user = addUser();

            usersFollow.get().put(user, new LinkedList<>());
            for (int j = 0 ; j < data.get(random.nextInt(bound)); j++) {
                localUsers.add(user);
            }
        }
        usersProbability.addAll(localUsers);
        latchDatabase.countDown();
        latchDatabase.await();

//        System.out.println("Following phase");
        //Following phase

        for (Long userA: usersFollow.get().keySet()){

            int nbFollow = data.get(random.nextInt(bound));
            for(int j = 0; j <= nbFollow; j++){
                n = random.nextInt(usersProbability.size());
                userB = usersProbability.get(n);

                followUser(userA, userB);
                usersFollow.get().get(userA).add(userB);
            }
        }
    }

    public long addUser() throws InvocationTargetException, InstantiationException, IllegalAccessException {

        long userID = next_user_ID.incrementAndGet();

        mapFollowers.put(userID, factory.getSet() );
        mapFollowing.put(userID, factory.getSet() );
        mapTimelines.put(userID, new Timeline(factory.getQueue()) );

        return userID;
    }

    // Adding user_A to the followers of user_B
    // and user_B to the following of user_A
    public void followUser(Long userA, Long userB){
        mapFollowers.get(userB).add(userA);
        mapFollowing.get(userA).add(userB);
    }

    // Removing user_A to the followers of user_B
    // and user_B to the following of user_A
    public void unfollowUser(Long userA, Long userB){
        mapFollowers.get(userB).remove(userA);
        mapFollowing.get(userA).remove(userB);
    }

    public void tweet(Long user, String msg){

        for (long follower : mapFollowers.get(user)) {
            mapTimelines.get(follower).add(msg);
        }
    }

    public void showTimeline(Long user){
        mapTimelines.get(user).read();
    }
    
}
