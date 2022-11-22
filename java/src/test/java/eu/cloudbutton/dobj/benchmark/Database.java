package eu.cloudbutton.dobj.benchmark;

import eu.cloudbutton.dobj.Factory;
import eu.cloudbutton.dobj.Timeline;
import eu.cloudbutton.dobj.incrementonly.Counter;
import eu.cloudbutton.dobj.incrementonly.CounterJUC;
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
    private final Factory factory;
    private final double alpha;
    private final int nbThread;
    private final Map<Long, Set<Long>> mapFollowers;
    private final Map<Long, Set<Long>> mapFollowing;
    private final Map<Long, Timeline<String>> mapTimelines;
    private final Counter next_user_ID;
    private final ThreadLocal<String> threadName;
    private final List<Long> usersProbability;
    private final ThreadLocal<List<Long>> localUsersProbability;
    private ThreadLocalRandom random;

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
        mapFollowers = new ConcurrentHashMap<>();
        mapFollowing = factory.getMap();
        mapTimelines = new ConcurrentHashMap<>();
        next_user_ID = factory.getCounter();
        threadName = ThreadLocal.withInitial(() -> Thread.currentThread().getName());
        usersProbability = new CopyOnWriteArrayList<>();
        localUsersProbability = ThreadLocal.withInitial(() -> new ArrayList<>());
        random = null;

    }

    public void fill(int nbUsers, CountDownLatch latchDatabase, Map<Long, Queue<Long>> usersFollow) throws InterruptedException, ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException {

        random = ThreadLocalRandom.current();

        int n, userPerThread;
        long user, userB;

        int bound = 1000;

        List<Integer> data = new DiscreteApproximate(1, alpha).generate(bound);
        int i = 0;

        double ratio = 100000 / 175000000.0; //10⁵ is ~ the number of follow max on twitter and 175_000_000 is the number of user on twitter (stats from the article)
        long max = (long) ((long) nbUsers * ratio);
        max = max == 0 ? 1 : max;


        for (int val: data){
            if (val >= max) {
                data.set(i, (int) max);
            }
            if (val < 1)
                data.set(i, 1);
            i++;
        }

        //adding all users

//        System.out.println("Adding users");

        userPerThread = nbUsers / nbThread;

        for (int id = 0; id < userPerThread; id++) {
            user = addUser();

            usersFollow.put(user, new LinkedList<>());

            for (int j = 0 ; j < data.get(random.nextInt(bound)); j++) {
                localUsersProbability.get().add(user);
            }
        }

        usersProbability.addAll(localUsersProbability.get());
        latchDatabase.countDown();
        latchDatabase.await();

/*        System.out.println();
        System.out.println("usersFollow from thread "+ Thread.currentThread().getName() +": " + usersFollow);
        System.out.println();*/
//        System.out.println("Following phase");
        //Following phase

        for (Long userA: usersFollow.keySet()){

            int nbFollow = data.get(random.nextInt(bound));
            for(int j = 0; j < nbFollow; j++){
                n = random.nextInt(usersProbability.size());
//                System.out.println("n => " + n);
//                TimeUnit.SECONDS.sleep(1);
                userB = 0;
                try{
                    userB = usersProbability.get(n);
                }catch (NullPointerException e){
                    System.exit(0);
                }

                followUser(userA, userB);
                usersFollow.get(userA).add(userB);
            }
        }


    }

    public long addUser() throws InvocationTargetException, InstantiationException, IllegalAccessException {

        long userID = next_user_ID.incrementAndGet();

        mapFollowers.put(userID, new ConcurrentSkipListSet<>());
        mapFollowing.put(userID, factory.getSet() );
        mapTimelines.put(userID, new Timeline(factory.getQueue()));

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

    public void tweet(Long user, String msg) throws InterruptedException {
        for (long follower : mapFollowers.get(user)) {
            mapTimelines.get(follower).add(msg);
        }
    }

    public void showTimeline(Long user) throws InterruptedException {
        mapTimelines.get(user).read();
    }
    
}
