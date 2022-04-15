package eu.cloudbutton.dobj.Benchmark;

import eu.cloudbutton.dobj.types.AbstractCounter;
import eu.cloudbutton.dobj.types.Factory;
import eu.cloudbutton.dobj.types.Timeline;
import lombok.Getter;
import nl.peterbloem.powerlaws.DiscreteApproximate;

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
    private final double alpha;
    private final ThreadLocalRandom random;
    private final AbstractMap<String, AbstractSet<String>> mapFollowers;
    private final AbstractMap<String, Timeline<String>> mapTimelines;
    private final AbstractCounter userID;
    private final List<String> usersProbability;


    public Database(String typeMap, String typeSet, String typeQueue, String typeCounter, double alpha) throws ClassNotFoundException {
        this.typeMap = typeMap;
        this.typeSet = typeSet;
        this.typeQueue = typeQueue;
        this.typeCounter = typeCounter;
        this.alpha = alpha;
        this.random = ThreadLocalRandom.current();
        mapFollowers = Factory.createMap(typeMap);
        mapTimelines = Factory.createMap(typeMap);
        userID = Factory.createCounter(typeCounter);
        usersProbability = new CopyOnWriteArrayList<>();
    }

    public void fill(int NB_USERS, double alpha, CountDownLatch latchDatabase, Map<String, List<String>> mapFollow) throws InterruptedException, ClassNotFoundException {
        int n;
        String userA, userB;

        int bound = 1000;

        List<Integer> data = new DiscreteApproximate(1, alpha).generate(bound);
        int i = 0;

        double ratio = 100000/ 175000000.0; //10⁵ is ~ the number of follow max on twitter and 175000000 is the number of user on twitter (stats from the article)
        long max = (long) ((long) NB_USERS * ratio);

        for (int val: data){
            if (val >= max) {
                data.set(i, (int) max);
            }
            if (val < 1)
                data.set(i, 1);
            i++;
        }

        //adding users
        for (int id = 0; id < NB_USERS; id++) {
            userID.increment();
            mapFollow.put("User_"+id, new ArrayList<>());

            for (int j = 0 ; j < data.get(random.nextInt(bound)); j++)
                usersProbability.add("User_"+id);

        }
        latchDatabase.countDown();
        latchDatabase.await();


        //Following phase
        for (int id = 0; id < NB_USERS; id++) {

            userA = "User_"+id;

            int nbFollow = data.get(random.nextInt(bound));
            for(int j = 0; j <= nbFollow; j++){
                n = random.nextInt(usersProbability.size());
                userB = usersProbability.get(n);

                followUser(userA, userB);
                mapFollow.get(userA).add(userB);
            }
        }

    }

    public void addUser(String user) throws ClassNotFoundException {
        mapFollowers.put(user,
                Factory.createSet(typeSet)
        );
        mapTimelines.put(user,
                new Timeline(Factory.createQueue(typeQueue),
                        Factory.createCounter(typeCounter)
                )
        );
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

    public Database localCopy() throws ClassNotFoundException {
        Database copyDatabase = new Database("Map", "Set", "Queue", "Counter", alpha);

        for (String user: this.getMapFollowers().keySet()){
            AbstractSet copySet = Factory.createSet(copyDatabase.getTypeSet());

            copySet.addAll(mapFollowers.get(user));
            copyDatabase.getMapFollowers().put(user, copySet);
        }

        for (String user: this.getMapTimelines().keySet()){
            Timeline<String> copyTimeline = new Timeline<>(Factory.createQueue(copyDatabase.getTypeQueue()), Factory.createCounter(copyDatabase.getTypeCounter()));

            for (String msg: mapTimelines.get(user).read()){
                copyTimeline.add(msg);
            }
            copyDatabase.getMapTimelines().put(user, copyTimeline);
        }

        return copyDatabase;
    }
}
