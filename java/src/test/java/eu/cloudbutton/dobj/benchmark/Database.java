package eu.cloudbutton.dobj.benchmark;

import eu.cloudbutton.dobj.Factory;
import eu.cloudbutton.dobj.Timeline;
import eu.cloudbutton.dobj.key.Key;
import eu.cloudbutton.dobj.key.KeyGenerator;
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

    public Database(String typeMap, String typeSet, String typeQueue, String typeCounter, double alpha, int nbThread, int nbUserMax, List<Integer> powerlawArray) throws ClassNotFoundException{

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
        queueUsers = new ConcurrentLinkedQueue<>();
        this.powerlawArray = powerlawArray;
        nbUsers = powerlawArray.size();
        keyGenerator = new SimpleKeyGenerator(nbUserMax);

        generateUsers();
    }

    public void fill(CountDownLatch latchDatabase, Map<Key, Queue<Key>> usersFollow) throws InterruptedException, ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException, OutOfMemoryError {

        random = ThreadLocalRandom.current();

        int userPerThread;
        long somme = 0;
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

            try{
                somme += data.get(id);
                user = queueUsers.poll();
                addUser(user);

                usersFollow.put(user, new LinkedList<>());

                localUsersProbability.get().put(somme, user);
            }catch (Exception e){
                e.printStackTrace();
                System.exit(1);
            }
        }

        latchDatabase.countDown();
        latchDatabase.await();

        while (queueUsers.size() != 0){
            user = queueUsers.poll();
            if (user != null){
                somme += 1;
                addUser(user);

                usersFollow.put(user, new LinkedList<>());
                localUsersProbability.get().put(somme, user);
            }
        }

        localUsersProbabilityRange.set(somme);

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

        for (Key userA: usersFollow.keySet()){
            int nbFollow = Math.min(powerlawArray.get(random.nextInt(nbUsers)), nbUsers);
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
            assert mapFollowers.get(userB).size() > 0 : userB + " from " + Thread.currentThread().getName() + " do not follow anyone.";
        }

    }

    public Key generateUser(){
        return keyGenerator.nextKey();
    }

    public void generateUsers(){
        int i = 0;
        long somme = 0;
        Set<Key> localSetUser = new HashSet<>();

//        System.out.println(powerlawArray);
        while (localSetUser.size() < powerlawArray.size()){
            Key user = generateUser();
            if (localSetUser.add(user)){
                somme += this.powerlawArray.get(i);
                usersProbability.put(somme, user);
                i++;
            }
        }

        queueUsers.addAll(localSetUser);
        usersProbabilityRange = somme;
    }

    public void addUser(Key user) throws ClassNotFoundException {

        if (!mapFollowers.containsKey(user)) {
            mapFollowers.put(user, new ConcurrentSkipListSet<>());
            mapTimelines.put(user, new Timeline(Factory.createQueue(typeQueue)));
            mapFollowing.put(user, new HashSet<>());
        }
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




