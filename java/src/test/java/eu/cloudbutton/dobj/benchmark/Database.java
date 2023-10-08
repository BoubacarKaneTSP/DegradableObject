package eu.cloudbutton.dobj.benchmark;

import eu.cloudbutton.dobj.Factory;
import eu.cloudbutton.dobj.utils.FactoryIndice;
import eu.cloudbutton.dobj.Timeline;
import eu.cloudbutton.dobj.key.Key;
import eu.cloudbutton.dobj.key.KeyGenerator;
import eu.cloudbutton.dobj.key.SimpleKeyGenerator;
import lombok.Getter;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.math3.distribution.ParetoDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.RandomGeneratorFactory;

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
    private final Map<Integer, Key> mapIndiceToKey;
    private final Map<Key, Integer> mapKeyToIndice;
    private final int[] reciprocalDegree;
    private final int[] inDegree;
    private final int[] outDegree;
    private int reciprocal = 0; // Number of nodes with a reciprocal degree bigger than 0
    private int out = 0; // Number of nodes with an out degree bigger than 0
    private int in = 0; //Number of nodes with an in degree bigger than 0
    private int edges_r = 0; // Number of reciprocal edges
    private int edges_d = 0; // Number of directed edges
    private int diag = 0;
    private float diag_sum_r_dist;
    private float diag_sum_d_dist;
    private final KeyGenerator keyGenerator;
    private final ConcurrentSkipListMap<Long, Key> usersFollowProbability;
    private final ThreadLocal<ConcurrentSkipListMap<Long,Key>> localUsersUsageProbability;
    private final ThreadLocal<Long> localUsersUsageProbabilityRange;
    private final List<List<Key>> listLocalUser;
    private final List<Map<Key,Integer>> mapUsersFollowing;
    private final Map<Key, AtomicInteger> mapNbFollowers;
    private final AtomicInteger count;
    private final FactoryIndice factoryIndice;
    private long usersFollowProbabilityRange;
    private List<Key> listAllUser;
    private final ThreadLocal<Random> random;
    ThreadLocal<Integer> threadID;
    Map<Integer, List<Integer>> mapUsageDistribution;
    private static final double SCALEUSAGE = 20; // Paramètre d'échelle de la loi de puissance
    private static final double SCALEFOLLOW = 1.0; // Paramètre d'échelle de la loi de puissance
    private static final double FOLLOWERSHAPE = 1.35; // Paramètre de forme de la loi de puissance
    private static final double FOLLOWINGSHAPE = 1.28; // Paramètre de forme de la loi de puissance


    public Database(String typeMap, String typeSet, String typeQueue, String typeCounter,
                    int nbThread, int nbUserInit, int nbUserMax) throws ClassNotFoundException, InterruptedException, ExecutionException {

        this.typeMap = typeMap;
        this.typeSet = typeSet;
        this.typeQueue = typeQueue;
        this.typeCounter = typeCounter;
        this.nbThread = nbThread;
        this.factoryIndice = new FactoryIndice(nbThread);
        this.random = ThreadLocal.withInitial(() -> new Random(94));

        if (typeMap.contains("Extended")){
            mapFollowers = Factory.createMap(typeMap, factoryIndice);
            mapFollowing = Factory.createMap(typeMap, factoryIndice);
            mapTimelines = Factory.createMap(typeMap, factoryIndice);
        }else{
            mapFollowers = Factory.createMap(typeMap, nbThread);
            mapFollowing = Factory.createMap(typeMap, nbThread);
            mapTimelines = Factory.createMap(typeMap, nbThread);
        }

        usersFollowProbability = new ConcurrentSkipListMap<>();
        localUsersUsageProbability = ThreadLocal.withInitial(ConcurrentSkipListMap::new);
        localUsersUsageProbabilityRange = ThreadLocal.withInitial(() -> 0L);
        nbUsers = nbUserInit;
//        keyGenerator = new RetwisKeyGenerator(nbUserMax, nbUserMax,10);
        keyGenerator = new SimpleKeyGenerator(nbUserMax);
        listLocalUser = new ArrayList<>();
        mapUsersFollowing = new ArrayList<>();
        count = new AtomicInteger();
        listAllUser = new ArrayList<>();
        mapNbFollowers = new ConcurrentHashMap<>();
        threadID = new ThreadLocal<>();

        mapIndiceToKey = new ConcurrentHashMap<>();
        mapKeyToIndice = new ConcurrentHashMap<>();
        reciprocalDegree = new int[nbUsers];
        inDegree = new int[nbUsers];
        outDegree = new int[nbUsers];

        List<Integer> powerLawArray = generateValues(nbUsers, nbUserMax, 600, SCALEUSAGE);

//        Collections.sort(powerLawArray);


        mapUsageDistribution = new ConcurrentHashMap<>();

        for (int i = 0; i < nbThread; i++) {
            mapUsageDistribution.put(i, new ArrayList<>());
        }

//        System.out.println(powerLawArray);

//        TimeUnit.SECONDS.sleep(5);
        for (int i = 0; i < nbUsers; i++) {
            mapUsageDistribution.get(i%nbThread).add(powerLawArray.get(i));
        }

        for (int i = 0; i < nbThread; i++) {
            listLocalUser.add(new ArrayList<>());
            mapUsersFollowing.add(new ConcurrentSkipListMap<>());
        }

//        System.out.println(mapUsageDistribution);

        System.out.println("generate user");

        generateUsers();

        addingPhase();

        followingPhase();

        saveGraph("graph_follower_retwis.txt", mapFollowers);
        saveGraph("graph_following_retwis.txt", mapFollowing);
    }

    public void fill(CountDownLatch latchAddUser, CountDownLatch latchHistogram) throws InterruptedException, ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException, OutOfMemoryError {
        System.out.println("start adding user phase thread : " + Thread.currentThread().getName());

        threadID.set(count.getAndIncrement());
        List<Key> users = listLocalUser.get(threadID.get());

        //adding all users
        List<Integer> powerLawArray = mapUsageDistribution.get(threadID.get());

        int powerLawArraySize = powerLawArray.size();

//        System.out.println(powerLawArray);
        long somme = 0;
        int g = 0;
        for (Key user : users) {
//            if (++g%nbUsers*0.05 == 0)
//                System.out.println(g);

            somme += powerLawArray.get(g++%powerLawArraySize)+1;
//            somme += 1; // Each user have the same probability to be chosen
            addOriginalUser(user);
            localUsersUsageProbability.get().put(somme, user);
        }
//        System.out.println(localUsersUsageProbability.get().keySet());

        localUsersUsageProbabilityRange.set(somme);

        System.out.println("Donne adding users");

        latchAddUser.countDown();
        latchAddUser.await();


//        followingTest(threadID);
//        followingPhase(threadID.get(), localUsersFollow);

        latchHistogram.countDown();

    }

    public Key generateUser(){
        return keyGenerator.nextKey();
    }

    public void generateUsers() throws InterruptedException {

        String cheminFichier = "nodes_info.txt";
        Set<Key> localSetUser = new TreeSet<>();
        int r_degree, o_degree, i_degree;
        List<Integer> powerLawArray = generateValues(nbUsers, nbUsers, 600, SCALEUSAGE);
        long somme = 0;

        try {
            File fichier = new File(cheminFichier);

            FileReader fileReader = new FileReader(fichier);

            BufferedReader bufferedReader = new BufferedReader(fileReader);
            bufferedReader.readLine();

            for (int i = 0; i < nbUsers;) {

                Key user = generateUser();
                if (localSetUser.add(user)) {
                    if (i % nbUsers * 0.05 == 0) {
                        System.out.println(i);
                    }

                    String[] degrees = bufferedReader.readLine().split(" ");

                    r_degree = (int) Double.parseDouble(degrees[0]);
                    i_degree = (int) Double.parseDouble(degrees[1]);
                    o_degree = (int) Double.parseDouble(degrees[2]);
                    mapIndiceToKey.put(i, user);
                    mapKeyToIndice.put(user,i);
                    reciprocalDegree[i] = r_degree;
                    inDegree[i] = i_degree;
                    outDegree[i] = o_degree;

                    somme += powerLawArray.get(i);
                    usersFollowProbability.put(somme, user);

                    if (r_degree>0)
                        reciprocal++;
                    if (i_degree>0)
                        in++;
                    if (o_degree>0)
                        out++;

                    i++;
                }
            }

            usersFollowProbabilityRange = somme;

            bufferedReader.close();
            fileReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        edges_r = reciprocal*2;
        edges_d = (in + out)/2;

        int diag_sum_r = 0, diag_sum_d = 0;


        for (int i = 0; i < nbUsers; i++) {
            if (reciprocalDegree[i] != 0)
                diag_sum_r += Math.pow(reciprocalDegree[i],2)/edges_r;

            if (inDegree[i] != 0 && outDegree[i] != 0){
                diag_sum_d += (inDegree[i]*outDegree[i]) / edges_d;
                diag += 1;
            }
        }

        diag_sum_r_dist = diag_sum_r / ((reciprocal * (reciprocal - 1)) / 2);
        diag_sum_d_dist = diag_sum_d/(out*in-diag);

    }

    /*   public void generateUsers() throws InterruptedException {

        Set<Key> localSetUser = new TreeSet<>();
        long sommeProba = 0;
        int nbFollowing, nbFollower;
        double maxFollower, maxFollowing;
        List<Integer> powerLawArray = generateValues(nbUsers, nbUsers, 1.3, SCALEUSAGE);

        if ((nbUsers*0.43)/100 <= 1)
            maxFollowing = 1;
        else
            maxFollowing = (nbUsers*0.43)/100;

        if ((nbUsers*8.4)/100 <= 1)
            maxFollower = 1;
        else
            maxFollower = (nbUsers*8.4)/100;

        List<Integer> listNbFollowing = generateValues(nbUsers, maxFollowing, FOLLOWINGSHAPE, SCALEFOLLOW);
//        System.out.println(listNbFollowing);
//        System.out.println("listNbFollowing");
        List<Integer> listNbFollower = generateValues(nbUsers, maxFollower, FOLLOWERSHAPE, SCALEFOLLOW);

//        System.out.println(listNbFollower);
//        System.out.println("listNbFollower");

        for (int i = 0; i < nbUsers;) {
            if(i%nbUsers*0.05 == 0)
                System.out.println(i);

            Key user = generateUser();
            if (localSetUser.add(user)){
                nbFollower = Math.max(1,listNbFollower.get(i));
//                nbFollowing =1;
                nbFollowing = Math.max(1,listNbFollowing.get(i));
//                System.out.println("Follower : "+ nbFollower + " | Following : " + nbFollowing);

                sommeProba += powerLawArray.get(i);
//                sommeProba += 1;

                usersFollowProbability.put(sommeProba, user);
                listLocalUser.get(i%nbThread).add(user);
                mapUsersFollowing.get(i%nbThread).put(user, nbFollowing);
                mapNbFollowers.put(user, new AtomicInteger(nbFollower));
                i++;
            }

        }

//        System.out.println(mapUsersFollowing.get(0).values());

        listAllUser.addAll(localSetUser);
        System.out.println("Done generating users");

        usersFollowProbabilityRange = sommeProba;
//        System.out.println(usersFollowProbabilityRange);
    }*/

    public static List<Integer> generateValues(int numValues, double desiredMaxValue, double SHAPE, double SCALE) throws InterruptedException {
        List<Double> doubleValues = new ArrayList<>();
        List<Integer> values = new ArrayList<>();

        RandomGenerator rand = RandomGeneratorFactory.createRandomGenerator(new Random(94));
        ParetoDistribution distribution = new ParetoDistribution(rand,SCALE,SHAPE);

        double maxGeneratedValue = 0;
        for (int i = 0; i < numValues; i++) {
            double randomValue = distribution.sample();
            doubleValues.add(randomValue);
            if (randomValue > maxGeneratedValue) {
                maxGeneratedValue = randomValue;
            }
        }

        double scaleFactor = desiredMaxValue / maxGeneratedValue;

        for (int i = 0; i < numValues; i++) {
            double scaledValue = doubleValues.get(i) * scaleFactor;
//            values.add((int) Math.round(doubleValues.get(i))+1);
            values.add((int) Math.round(scaledValue)+1);
//            System.out.println((int) Math.round(doubleValues.get(i))+1);
        }

//        Collections.sort(values);
//        System.out.println(values);
//        System.out.println("aaa");
//        TimeUnit.SECONDS.sleep(5);
//        System.out.println(Collections.max(values));
        return values;
    }

    public void followingTest(int threadID) throws InterruptedException { // Each user follow only one user at first
        List<Key> users = listLocalUser.get(threadID);
        ThreadLocal<Random> random = ThreadLocal.withInitial(Random::new);

        for (Key userA: users){
            long val = random.get().nextLong()%usersFollowProbabilityRange;
            Key userB = usersFollowProbability.ceilingEntry(val).getValue();
            followUser(userA, userB);
        }
    }

    @FunctionalInterface
    interface MyCallableWithArgument {
        Void call(Integer argument) throws Exception;
    }

    public void followingPhase() throws InterruptedException, ExecutionException {
        System.out.println("start following phase thread : " + Thread.currentThread().getName());

        int nbProcess = Runtime.getRuntime().availableProcessors();
//        int nbProcess = 48;
        ExecutorService executorService = Executors.newFixedThreadPool(nbProcess);
        List<Future<Void>> futures = new ArrayList<>();

        System.out.println("nb process : " + nbProcess);
        
        MyCallableWithArgument myCallable = (Integer i) -> {

            int a, counter = 0, directed_sum = 0;
            float pr;
            Key userA, userB;

//            if(i%(nbUsers*0.001) == 0 || i<=nbProcess)
                System.out.println(i + " : " + Thread.currentThread().getName());

            // Sampling of reciprocal edges
            for (int j = i; j < nbUsers; j++) {

//                if(j%(nbUsers*0.25) == 0)
//                    System.out.println(j + " : " + Thread.currentThread().getName() + " | reciprocal");

                if (reciprocalDegree[j] != 0 && reciprocalDegree[i] != 0){
                    pr = 2*reciprocalDegree[i]*reciprocalDegree[j]/edges_r + diag_sum_r_dist;

                    if (pr>1)
                        pr = 1;

                    a = Math.random() < pr ? 1 : 0;

                    if (a==1){

                        userA = mapIndiceToKey.get(i);
                        userB = mapIndiceToKey.get(j);

                        followUser(userA,userB);
//                        localUsersFollow.get(userA).add(userB);

                        followUser(userB,userA);
//                        localUsersFollow.get(userB).add(userA);

                        if (inDegree[i] != 0 && outDegree[j] != 0){
                            counter++;
                            directed_sum += inDegree[i]*outDegree[j]/edges_d +diag_sum_d_dist;
                        }

                        if (inDegree[j] != 0 && outDegree[i] != 0){
                            counter++;
                            directed_sum += inDegree[j]*outDegree[i]/edges_d +diag_sum_d_dist;
                        }
                    }
                }
            }

            int sampled_reciprocal = directed_sum/(out*in-diag-counter);

            // Sampling of directed edges
            for (int j = i; j < nbUsers; j++) {

//                if(j%(nbUsers*0.25) == 0)
//                    System.out.println(j + " : " + Thread.currentThread().getName() + " | directed");

                if (inDegree[i] != 0 && outDegree[j] != 0){
                    pr = inDegree[i]*outDegree[j]/edges_d + diag_sum_d_dist + sampled_reciprocal;

                    if (pr>1)
                        pr = 1;

                    a = Math.random() < pr ? 1 : 0;

                    if (a==1){
                        userA = mapIndiceToKey.get(i);
                        userB = mapIndiceToKey.get(j);

                        followUser(userB, userA);
//                        localUsersFollow.get(userB).add(userA);
                    }
                }

                if (inDegree[j] != 0 && outDegree[i] != 0){
                    pr = inDegree[j]*outDegree[i]/edges_d + diag_sum_d_dist + sampled_reciprocal;

                    if (pr>1)
                        pr = 1;

                    a = Math.random() < pr ? 1 : 0;

                    if (a==1){
                        userA = mapIndiceToKey.get(i);
                        userB = mapIndiceToKey.get(j);

                        followUser(userA, userB);
//                        localUsersFollow.get(userA).add(userB);
                    }
                }
            }

            return null;
        };

        for (int i = 0; i < nbUsers; i++) {
            int finalI = i;
            Callable<Void> callable = () -> myCallable.call(finalI);
            futures.add(executorService.submit(callable));
        }

        System.out.println("launch futures");

        for (Future<Void> future :futures){
            future.get();
        }

        System.out.println("end following phase thread : " + Thread.currentThread().getName());
    }

    public void addingPhase() throws ExecutionException, InterruptedException {

        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Future<Void>> futures = new ArrayList<>();

        AtomicInteger somme = new AtomicInteger();
        List<Integer> powerLawArray = generateValues(nbUsers, nbUsers, 600, SCALEUSAGE);

        Collections.sort(powerLawArray);

        MyCallableWithArgument myCallable = (Integer i) -> {

            Key user;

            somme.addAndGet(powerLawArray.get(i));

            user = mapIndiceToKey.get(i);
            addOriginalUser(user);
            localUsersUsageProbability.get().put(somme.longValue(), user);
            localUsersUsageProbabilityRange.set(
                    localUsersUsageProbabilityRange.get()
                            + somme.longValue());

            listLocalUser.get(i%nbThread).add(user);
            return null;
        };

        for (int i = 0; i < nbUsers; i++) {
            int finalI = i;
            Callable<Void> callable = () -> myCallable.call(finalI);
            futures.add(executorService.submit(callable));

        }

        for (Future<Void> future :futures){
            future.get();
        }
    }

    private void saveGraph(String fileName, Map<Key,Set<Key>> map){

        String line = "";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {

            for (Key user : mapIndiceToKey.values()){
                line += mapKeyToIndice.get(user).toString();

                for (Key follower: map.get(user)){
                    line += " " + mapKeyToIndice.get(follower);
                }

                line += "\n";
            }

            writer.write(line);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /*
    public void followingPhase(int threadID, Map<Key, Queue<Key>> localUsersFollow) throws InterruptedException {
        System.out.println("start following phase thread : " + Thread.currentThread().getName());

        List<Key> users = listLocalUser.get(threadID);
        int nbLocalUser = users.size();
        int j = 0;
        long randVal;

        for (Key userA: users){
            if(++j%(nbUsers*0.05) == 0)
                System.out.println(j);

            Queue<Key> usersFollow = localUsersFollow.get(userA);
            int nbFollow = Math.min(mapUsersFollowing.get(threadID).get(userA), nbLocalUser);
//	        System.out.println(nbFollow);

//            for (Long v : usersFollowProbability.keySet())
//                System.out.println(v + " => " + usersFollowProbability.get(v));
//            System.out.println(usersFollowProbability);
            for (long i = 0; i < nbFollow;) {


//                System.out.println(i + " | " + nbFollow);

                randVal = Math.abs(random.get().nextLong() % usersFollowProbabilityRange);
                Key userB =  usersFollowProbability.ceilingEntry(randVal).getValue();

//                randVal = random.get().nextInt(nbLocalUser);
//                Key userB = users.get((int) randVal);

                assert userB != null : "User generated is null";

                if (!usersFollow.contains(userB) && mapNbFollowers.get(userB).getAndDecrement() > 0) {
                    followUser(userA, userB);
                    usersFollow.add(userB);
                    i++;
                }
//                if (mapNbFollowers.get(userB).getAndDecrement() > 0) {
//                }else
//                    nbFailFollow++;
//
//                if (nbFailFollow >= nbUsers)
//                    break;
            }

        }

//        System.out.println();
//        for (Key user: users){
//            if (mapFollowers.get(user).size() > 500) {
//                System.out.println(user + " size followers => " + mapFollowers.get(user).size());
//                System.out.println();
//            }

//            if (mapFollowing.get(user).size() > 500) {
//                System.out.println(user + "size following = >" + mapFollowing.get(user).size());
//                System.out.println();
//            }


//        }

//        TimeUnit.SECONDS.sleep(5);
        System.out.println("end following phase thread : " + Thread.currentThread().getName());
    }*/

    public String computeHistogram(int range, int max, String type){

        Map<Key, Set<Key>> computedMap = null;

        if (type.equals("Follower")) {
            computedMap = mapFollowers;
        }else if (type.equals("Following")){
            computedMap = mapFollowing;
        }

//        for (int i = 0; i <= max; i+=max/range) {
//            mapHistogram.put(i,0);
//        }

        int v,k;

        assert computedMap != null : "Failed initialize map while computing histogram";

        String values = "";
        for (Set<Key> s : computedMap.values()) {
            values += s.size() +" ";
//            k = mapHistogram.ceilingKey(s.size());
//            v = mapHistogram.get(k) + 1;
//            mapHistogram.put(k, v);
        }

//        int totalUser = 0;
//
//        for (int nb : mapHistogram.values())
//            totalUser += nb;
//
//        assert  totalUser == nbUsers : "Wrong number of user in histogram";

        return values;
//        return mapHistogram;
    }
    public Map computeFollowHistogram(int range, int max, String type){

//        NavigableMap<Integer,Integer> mapHistogram = new TreeMap<>();
        Map<Key, Set<Key>> computedMap = null;
        Map<Integer, Integer> map = new HashMap<>();

        if (type.equals("Follower")) {
            computedMap = mapFollowers;
        }else if (type.equals("Following")){
            computedMap = mapFollowing;
        }


        for (Key key : computedMap.keySet()){
//            System.out.println(key);
            int size = computedMap.get(key).size();
//            if (size > 1000)
//                System.out.println(size);
            if (!map.containsKey(size))
                map.put(size, 1);
            else
                map.put(size, map.get(size) + 1);
        }

        return map;
    }

    public double computeAvgCoefficientCluster(){

        System.out.println("Computing AvgCoefficientCluster");
        double avg = 0;

        Set<Key> setUsers = mapFollowers.keySet();
        Set<Key> possibleNeighbors;

        int count = 0;

        for (Key usr : setUsers){

            if (++count%(nbUsers*0.05) == 0)
                System.out.println("nb usr processed : " + count);

            possibleNeighbors = new HashSet<>(){
                {
                    addAll(mapFollowers.get(usr));
                    addAll(mapFollowing.get(usr));
                }
            };

            List<Key> neighbors = new ArrayList<>();

            for (Key possibleNeighbor : possibleNeighbors){
                if (hasEdge(usr, possibleNeighbor))
                    neighbors.add(possibleNeighbor);
            }

            int numLinks = 0;
            int neighborSize = neighbors.size();

            if (neighborSize < 2)
                continue;


            for (int i = 0; i <neighborSize; i++) {
                for (int j = i+1; j < neighborSize; j++) {
                    if (hasEdge(neighbors.get(i), neighbors.get(j)))
                        numLinks++;
                }
            }

            double coefficient_cluster = (2.0 * numLinks) / (neighborSize * (neighborSize - 1));

            if (coefficient_cluster>0)
                System.out.println("CC : " + coefficient_cluster);
            avg += coefficient_cluster;
        }

        avg = avg / setUsers.size();

        return avg;
    }

    private boolean hasEdge(Key usr1, Key usr2){
        if (usr1.equals(usr2))
            return false;

        return mapFollowers.get(usr1).contains(usr2) && mapFollowers.get(usr2).contains(usr1);
    }

    private void generateGraph(){

    }

    public void addOriginalUser(Key user) throws ClassNotFoundException {
//        mapFollowers.put(user, new ConcurrentSkipListSet<>());
//        mapFollowers.put(user, new HashSet<>());
        mapFollowing.put(user, new HashSet<>());

        if (typeSet.contains("Extended"))
            mapFollowers.put(user, Factory.createSet(typeSet, factoryIndice));
        else
            mapFollowers.put(user, Factory.createSet(typeSet, nbThread));
        mapTimelines.put(user, new Timeline(Factory.createQueue(typeQueue)));
    }

    public void addUser(Key user, Set<Key> dummySet, Timeline<String> dummyTimeline) {
//        assert user != null : "User is null";
//        assert dummySet != null : "Set is null";
//        assert dummyTimeline != null : "Timeline is null";

        mapFollowers.put(user,dummySet);
        mapFollowing.put(user, dummySet);
        mapTimelines.put(user, dummyTimeline);
    }

    public void removeUser(Key user){
        mapFollowers.remove(user);
        mapFollowing.remove(user);
        mapTimelines.remove(user);
    }

    // Adding user_A to the followers of user_B
    // and user_B to the following of user_A
    public void followUser(Key userA, Key userB) throws InterruptedException {

        mapFollowers.get(userB)
                .add(userA);
        mapFollowing.get(userA)
                .add(userB);

//        System.out.println(Thread.currentThread().getName() + " is making " + userA + " follows " + userB + " and it has indice : " + ((SegmentAware)userB).getReference().get());

//        TimeUnit.SECONDS.sleep(100000);

//        if (set.add(userB)){
////            System.out.println(Thread.currentThread().getName() + " add first " + userB + " to the set of following of " + userA);
//        }else{
////            System.out.println(Thread.currentThread().getName() + " add " + userB + " in the wrong set");
//            System.exit(0);
//        }

    }

    // Removing user_A to the followers of user_B
    // and user_B to the following of user_A
    public void unfollowUser(Key userA, Key userB){
        mapFollowers.get(userB)
                .remove(userA);
        mapFollowing.get(userA)
                .remove(userB);
    }

    public void tweet(Key user, String msg) throws InterruptedException {
        Set<Key> set = mapFollowers.get(user);

//        System.out.println(set.size());
        for (Key follower : set) {
            Timeline timeline = mapTimelines.get(follower);
//
            timeline.add(msg);
            break;
        }
    }

    public void showTimeline(Key user) throws InterruptedException {
        mapTimelines.get(user).read();
    }

    public class generateGraph implements Callable<Void> {

        @Override
        public Void call() throws Exception {

            return null;
        }
    }
}