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
    private ThreadLocal<Random> random;
    private final KeyGenerator keyGenerator;
    private final ConcurrentSkipListMap<Long, Key> usersProbability;
    private final ThreadLocal<ConcurrentSkipListMap<Long,Key>> localUsersProbability;
    private final Queue<Key> queueUsers;
    private long usersProbabilityRange;
    private final ThreadLocal<Long> localUsersProbabilityRange;
    private final List<Integer> inPowerlawArrayFollowers;
    private final List<Integer> outPowerlawArrayFollowers;
    private final List<Integer> powerlawArrayUsers;
    private final List<List<Key>> listLocalUser;
    private final List<Map<Key,Integer>> mapUsersFollowing;
    private final AtomicInteger count;
    private final FactoryIndice factoryIndice;
    private ArrayList<Double> ListNbFollowing = new ArrayList<>(Arrays.asList(0.005, 0.003, 0.0015 , 0.0007, 0.0004, 0.0002, 0.0001, 0.00005, 0.00002, 0.00001));
    private ArrayList<Double> ListNbFollower = new ArrayList<>(Arrays.asList(0.1, 0.06, 0.03, 0.014, 0.008, 0.004, 0.002, 0.001, 0.0004, 0.0002));

    public Database(String typeMap, String typeSet, String typeQueue, String typeCounter,
                    int nbThread, int nbUserInit, int nbUserMax,
                    List<Integer> inPowerlawArrayFollowers, List<Integer> outPowerlawArrayFollowers, List<Integer> powerlawArrayUsers) throws ClassNotFoundException{

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

        usersProbability = new ConcurrentSkipListMap<>();
        localUsersProbability = ThreadLocal.withInitial(ConcurrentSkipListMap::new);
        localUsersProbabilityRange = new ThreadLocal<>();
        random = null;
        queueUsers = new ConcurrentLinkedQueue<>();
        this.inPowerlawArrayFollowers = inPowerlawArrayFollowers;
        this.outPowerlawArrayFollowers = outPowerlawArrayFollowers;
        this.powerlawArrayUsers = powerlawArrayUsers;
        nbUsers = nbUserInit;
        keyGenerator = new SimpleKeyGenerator(nbUserMax);
        listLocalUser = new ArrayList<>();
        mapUsersFollowing = new ArrayList<>();
        count = new AtomicInteger();


        for (int i = 0; i < nbThread; i++) {
            listLocalUser.add(new ArrayList<>());
            mapUsersFollowing.add(new ConcurrentSkipListMap<>());
        }

        generateUsers();
    }

    public void fill(CountDownLatch latchDatabase,  Map<Key, Queue<Key>> localUsersFollow) throws InterruptedException, ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException, OutOfMemoryError {

        random = ThreadLocal.withInitial(Random::new);

//        System.out.println("start adding user phase thread : " + Thread.currentThread().getName());

        long somme = 0;
        Key user;
        int threadID = count.getAndIncrement();
        List<Key> users = listLocalUser.get(threadID);

        //adding all users

        for (Key key : users) {
            somme += powerlawArrayUsers.get(random.get().nextInt(powerlawArrayUsers.size()));
            user = key;
            addUser(user);
            localUsersProbability.get().put(somme, user);
            localUsersFollow.put(user, new LinkedList<>());
        }

        localUsersProbabilityRange.set(somme);

        latchDatabase.countDown();
        latchDatabase.await();

        //Following phase

//        System.out.println("start following phase thread : " + Thread.currentThread().getName());

        int y = 0;
        int nbLocalUsers = users.size();

        for (Key userA: users){

            int nbFollow = (int) (ListNbFollowing.get((int) (y/(nbLocalUsers*0.1))) * nbUsers);
            nbFollow = nbFollow > 0 ? nbFollow : 1;
            int j = 0;
            boolean flagFollowMax;
            int nbUserIterated;

            while (j < nbFollow){
                nbUserIterated = 0;
                flagFollowMax = true;
                for (Key userB: mapUsersFollowing.get(threadID).keySet()){
                    nbUserIterated++;

                    int nbFollowingLeft = mapUsersFollowing.get(threadID).get(userB);

                    if (nbFollowingLeft > 0){
                        followUser(userA, userB);
                        localUsersFollow.get(userA).add(userB);
                        flagFollowMax = false;
                        mapUsersFollowing.get(threadID).put(userB, nbFollowingLeft - 1);
                        j++;
                    }else{
                        mapUsersFollowing.get(threadID).remove(userB);
                    }
                    if (j >= nbFollow)
                        break;
                }

                if (flagFollowMax && nbUserIterated == mapUsersFollowing.get(threadID).size())
                    break;

            }

//            Set set = mapFollowers.get(userB);
//            assert set.size() > 0 : userB + " from " + Thread.currentThread().getName() + " do not follow anyone.";
            y++;
        }


//        System.out.println("end following phase thread : " + Thread.currentThread().getName());


    }

    public Key generateUser(){
        return keyGenerator.nextKey();
    }

    public void generateUsers(){

        Set<Key> localSetUser = new ConcurrentSkipListSet<>();
        Random random = new Random();
        long somme = 0;

        for (int i = 0; i < nbUsers;) {
            Key user = generateUser();
            if (localSetUser.add(user)){
                int nbFollower = (int) (ListNbFollower.get((int) (i/(nbUsers*0.1)))*nbUsers);
                somme += nbFollower;
                usersProbability.put(somme, user);
                listLocalUser.get(random.nextInt(nbThread)).add(user);
                mapUsersFollowing.get(i%nbThread).put(user, nbFollower);
                i++;
            }

        }

        usersProbabilityRange = somme;

        /*int i = 0;
        long somme = 0;
        Collections.sort(inPowerlawArrayFollowers);

        while (localSetUser.size() < nbUsers){
            Key user = generateUser();
            if (localSetUser.add(user)){
                somme += this.inPowerlawArrayFollowers.get(i % inPowerlawArrayFollowers.size());
                i++;
            }
        }

        usersProbabilityRange = somme;*/
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




