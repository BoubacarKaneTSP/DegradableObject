package eu.cloudbutton.dobj.Benchmark;

import eu.cloudbutton.dobj.counter.AbstractCounter;
import eu.cloudbutton.dobj.counter.BoxLong;
import eu.cloudbutton.dobj.Factory;
import eu.cloudbutton.dobj.Timeline;
import lombok.Getter;
import nl.peterbloem.powerlaws.DiscreteApproximate;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
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
    private final ThreadLocal<String> threadName;
    private final List<Long> usersProbability;

    ///////////////////////////////////////

    private final AbstractMap<String, Long> users; // Associate an userID for each user
    private final AbstractMap<Long,AbstractQueue<Long>> posts; // Stores for each user a queue containing all the posts
    private final AbstractMap<Long, AbstractMap<String, String>> mapUser; // Stores the information for each user
    private final AbstractMap<Long, AbstractMap<String, String>> mapPost; // Stores the information for each post
    private final AbstractMap<Long, AbstractSet<Long>> followers; // Stores the followers for each users
    private final AbstractMap<Long, AbstractSet<Long>> following; // Stores the following for each users
    private final AbstractQueue<Long> timeline; // Stores the last 1000 post posted. We need this queue to conserve the order of the posts
    private final AbstractCounter next_user_ID; // A counter generating a unique ID for each user
    private final AbstractCounter next_post_ID; // A counter generating a unique ID for each post
    private final ThreadLocal<BoxLong> head_or_tail;

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
        threadName = ThreadLocal.withInitial(() -> Thread.currentThread().getName());
        usersProbability = new CopyOnWriteArrayList<>();

        /////////////////////////////
        users = factory.getMap();
        posts = factory.getMap();
        mapUser = factory.getMap();
        mapPost = factory.getMap();
        followers = factory.getMap();
        following = factory.getMap();
        timeline = new ConcurrentLinkedQueue<>(); // We cannot use the DegradableQueue here since many thread may use poll()
        next_user_ID = factory.getCounter();
        next_post_ID = factory.getCounter();
        head_or_tail = ThreadLocal.withInitial(BoxLong::new);
        head_or_tail.get().setVal(0);
    }

    public void fill(int nbUsers, CountDownLatch latchDatabase, ThreadLocal<Map<Long, Queue<Long>>> usersFollow) throws InterruptedException, ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException {
        int n, userPerThread;
        Long user, userB;

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

        System.out.println("Following phase");
//        Following phase

        int usersProbabilitySize = usersProbability.size();
        for (Long userA: usersFollow.get().keySet()){

            int nbFollow = data.get(random.nextInt(bound));
            for(int j = 0; j <= nbFollow; j++){
                n = random.nextInt(usersProbabilitySize);
                userB = usersProbability.get(n);

                followUser(userA, userB);
                usersFollow.get().get(userA).add(userB);
            }
        }

//        System.out.println(Thread.currentThread().getName() + " : " +val/m);


    }

    public Long addUser() throws InvocationTargetException, InstantiationException, IllegalAccessException {

        // Generating a unique ID, a username and a password for the new user
        long ID = next_user_ID.incrementAndGet();
        String username = Long.toString(ID);
        String password = Integer.toString(username.hashCode());

        users.put(username, ID);

        AbstractMap<String,String> infoUser = factory.getMap();
        infoUser.put("username", username);
        infoUser.put("password", password);

        // Adding the user in the database
        mapUser.put(ID, infoUser);

        // Generate a queue for storing the post of the new user
        // and two set for storing the followers and the following of the new user
        posts.put(ID, factory.getQueue());
        followers.put(ID, factory.getSet());
        following.put(ID, factory.getSet());

        return ID;
    }

    public void followUser(Long userA_ID, Long userB_ID){ // userA follow userB

        // Adding user_A to the followers of user_B
        // and user_B to the following of user_A
        followers.get(userB_ID).add(userA_ID);
//        following.get(userA_ID).add(userB_ID);
    }

    public void unfollowUser(Long userA_ID, Long userB_ID){ // userA unfollow userB

        // Removing user_A to the followers of user_B
        // and user_B to the following of user_A
        followers.get(userB_ID).remove(userA_ID);
//        following.get(userA_ID).remove(userB_ID);
    }

    public void tweet(Long user_ID, String msg) throws InvocationTargetException, InstantiationException, IllegalAccessException {

        // Generate a new ID for the new post and getting the ID for the user
        Long post_ID = next_post_ID.incrementAndGet();

        // Create the post
        AbstractMap<String, String> infoMsg = factory.getMap();
        infoMsg.put("userID", Long.toString(user_ID));
        infoMsg.put("time", Long.toString(System.nanoTime()));
        infoMsg.put("body", msg);

        // Add the post to the map storing all the post
        mapPost.put(post_ID, infoMsg);

        // Getting the followers of the user

        // Posting in the timeline of every followers
        for (Long follower : followers.get(user_ID)) {
            posts.get(follower).add(post_ID);
        }

        // The user post the message to its own timeline
        posts.get(user_ID).add(post_ID);

        // Adding the post in the "general" timeline
        timeline.offer(post_ID);
        if (timeline.size() > 1000) // with this method, we have at most 1000 + nbThread posts stored in the timeline
            timeline.poll();
    }

    public void read(Long user_ID){

        if (head_or_tail.get().getVal()%2 == 0){ // We print the timeline and the users' post every other time
            showTimeline();
        }else{
            showUsersPost(user_ID);
        }

    }

    public void showTimeline(){
        for (Long post_ID: timeline){
            showPost(post_ID);
        }
    }

    public void showUsersPost(Long user_ID){
        for (Long post_ID : posts.get(user_ID)){
            showPost(post_ID);
        }
    }

    public void showPost(Long post_ID){

        String username, body, strPost_Id;
        Long elapsedTime, user_ID;

        user_ID = Long.parseLong(mapPost.get(post_ID).get("userID"));
        username = mapUser.get(user_ID).get("username");
        body = mapPost.get(post_ID).get("body");
        elapsedTime = System.nanoTime() - Long.parseLong(mapPost.get(post_ID).get("time"));
    }
}
