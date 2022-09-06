package eu.cloudbutton.dobj.Benchmark;

import eu.cloudbutton.dobj.incrementonly.BoxLong;
import eu.cloudbutton.dobj.Factory;
import eu.cloudbutton.dobj.Timeline;
import lombok.Getter;
import nl.peterbloem.powerlaws.DiscreteApproximate;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
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
    private final AbstractMap<String, AbstractSet<String>> mapFollowers;
    private final AbstractMap<String, Timeline<String>> mapTimelines;
    private final ThreadLocal<BoxLong> userID;
    private final ThreadLocal<String> threadName;
    private final List<String> usersProbability;

    public Database(String typeMap, String typeSet, String typeQueue, String typeCounter, double alpha, int nbThread) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        this.factory = new Factory();
        Class cls;

        try{
            cls = Class.forName("eu.cloudbutton.dobj.counter."+typeCounter);
        }catch (ClassNotFoundException e){
            cls = Class.forName("java.util.concurrent."+typeCounter);
        }

        factory.setFactoryCounter(cls);

        try{
            cls = Class.forName("eu.cloudbutton.dobj.set."+typeSet);
        }catch (ClassNotFoundException e){
            cls = Class.forName("java.util.concurrent."+typeSet);
        }

        factory.setFactorySet(cls);

        try{
            cls = Class.forName("eu.cloudbutton.dobj.queue."+typeQueue);
        }catch (ClassNotFoundException e){
            cls = Class.forName("java.util.concurrent."+typeQueue);
        }

        factory.setFactoryQueue(cls);

        try{
            cls = Class.forName("eu.cloudbutton.dobj.map."+typeMap);
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
        mapFollowers = factory.getMap();
        mapTimelines = factory.getMap();
        userID = ThreadLocal.withInitial(() -> new BoxLong());
        threadName = ThreadLocal.withInitial(() -> Thread.currentThread().getName());
        usersProbability = new CopyOnWriteArrayList<>();
    }

    public void fill(int nbUsers, CountDownLatch latchDatabase, ThreadLocal<Map<String, Queue<String>>> usersFollow) throws InterruptedException, ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException {
        int n, userPerThread;
        String user, userB;

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

        List<String> localUsers = new ArrayList<>();
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

        for (String userA: usersFollow.get().keySet()){

            int nbFollow = data.get(random.nextInt(bound));
            for(int j = 0; j <= nbFollow; j++){
                n = random.nextInt(usersProbability.size());
                userB = usersProbability.get(n);

                followUser(userA, userB);
                usersFollow.get().get(userA).add(userB);
            }
        }

//        System.out.println(Thread.currentThread().getName() + " : " +val/m);


    }

    public String addUser() throws InvocationTargetException, InstantiationException, IllegalAccessException {

        long val = userID.get().getVal();

        String user = "User_" + threadName.get() + "_" + val;
        userID.get().setVal(val + 1);

        mapFollowers.put(user,
                factory.getSet()
        );
        mapTimelines.put(user,
                new Timeline(factory.getQueue(),
                        factory.getCounter()
                )
        );

        return user;

    }

    public void followUser(String userA, String userB){
        mapFollowers.get(userB).add(userA);
    }

    public void unfollowUser(String userA, String userB){
        mapFollowers.get(userB).remove(userA);
    }

    public void tweet(String user, String msg){

        for (String follower : mapFollowers.get(user)) {
            mapTimelines.get(follower).add(msg);
        }
    }

    public void showTimeline(String user){
        mapTimelines.get(user).read();
    }
    
}
