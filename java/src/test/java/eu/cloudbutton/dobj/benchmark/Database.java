package eu.cloudbutton.dobj.benchmark;

import eu.cloudbutton.dobj.Factory;
import eu.cloudbutton.dobj.Profile;
import eu.cloudbutton.dobj.set.ConcurrentHashSet;
import eu.cloudbutton.dobj.utils.FactoryIndice;
import eu.cloudbutton.dobj.Timeline;
import eu.cloudbutton.dobj.key.Key;
import eu.cloudbutton.dobj.key.KeyGenerator;
import eu.cloudbutton.dobj.key.SimpleKeyGenerator;
import lombok.Getter;

import java.io.*;
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
    private Map<Key, Set<Key>> mapFollowers;
    private Map<Key, Set<Key>> mapFollowing;
    private Map<Key, Timeline<String>> mapTimelines;
    private Map<Key, Integer> mapProfiles;
    private Set<Key> community;
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
    private long usersFollowProbabilityRange;
    private final Map<Integer, ConcurrentSkipListMap<Long,Key>> localUsersUsageProbability;
    private final Map<Integer, Long> localUsersUsageProbabilityRange;
    private final List<List<Key>> listLocalUser;
    private final Map<Integer, Map<Key, Queue<Key>>> listLocalUsersFollow;
    private final AtomicInteger count;
    private final FactoryIndice factoryIndice;
    private final ThreadLocal<Random> random;
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
        this.factoryIndice = new FactoryIndice(nbThread + 1); // +1 because a different thread add all users at first
        this.random = ThreadLocal.withInitial(() -> new Random(94));

        if (typeMap.contains("Extended")){
            mapFollowers = Factory.createMap(typeMap, factoryIndice);
            mapFollowing = Factory.createMap(typeMap, factoryIndice);
            mapTimelines = Factory.createMap(typeMap, factoryIndice);
            mapProfiles = Factory.createMap(typeMap, factoryIndice);
        }else{
            mapFollowers = Factory.createMap(typeMap, nbThread);
            mapFollowing = Factory.createMap(typeMap, nbThread);
            mapTimelines = Factory.createMap(typeMap, nbThread);
            mapProfiles = Factory.createMap(typeMap, nbThread);
        }

        if (typeSet.contains("Extended")){
            community = Factory.createSet(typeSet, factoryIndice);
        }else{
            community = Factory.createSet(typeSet, nbThread);
        }


        usersFollowProbability = new ConcurrentSkipListMap<>();
        localUsersUsageProbability = new ConcurrentHashMap<>();
        localUsersUsageProbabilityRange = new ConcurrentHashMap<>();
        nbUsers = nbUserInit;
//        keyGenerator = new RetwisKeyGenerator(nbUserMax, nbUserMax,10);
        keyGenerator = new SimpleKeyGenerator(nbUserMax);
        listLocalUser = new ArrayList<>();
        listLocalUsersFollow = new ConcurrentHashMap<>();
        count = new AtomicInteger();

        mapIndiceToKey = new ConcurrentHashMap<>();
        mapKeyToIndice = new ConcurrentHashMap<>();
        reciprocalDegree = new int[nbUsers];
        inDegree = new int[nbUsers];
        outDegree = new int[nbUsers];

        for (int i = 0; i < nbThread; i++) {
            localUsersUsageProbability.put(i , new ConcurrentSkipListMap<>());
            localUsersUsageProbabilityRange.put(i, 0L);
            listLocalUsersFollow.put(i, new HashMap<>());
            listLocalUser.add(new ArrayList<>());
        }

//        System.out.println("generate user");

//        generateUsers();

//        addingPhase();

//        followingPhase();

//        saveGraph("graph_follower_retwis.txt", mapFollowers);
//        saveGraph("graph_following_retwis.txt", mapFollowing);

//        loadGraph();
//        loadCompleteGraph();
        loadDAPGraph();
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

        return values;
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


            // Sampling of reciprocal edges
            for (int j = i; j < nbUsers; j++) {

                if (reciprocalDegree[j] != 0 && reciprocalDegree[i] != 0){
                    pr = 2*reciprocalDegree[i]*reciprocalDegree[j]/edges_r + diag_sum_r_dist;

                    if (pr>1)
                        pr = 1;

                    a = Math.random() < pr ? 1 : 0;

                    if (a==1){

                        userA = mapIndiceToKey.get(i);
                        userB = mapIndiceToKey.get(j);

                        followUser(userA,userB);

                        followUser(userB,userA);

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

                if (inDegree[i] != 0 && outDegree[j] != 0){
                    pr = inDegree[i]*outDegree[j]/edges_d + diag_sum_d_dist + sampled_reciprocal;

                    if (pr>1)
                        pr = 1;

                    a = Math.random() < pr ? 1 : 0;

                    if (a==1){
                        userA = mapIndiceToKey.get(i);
                        userB = mapIndiceToKey.get(j);

                        followUser(userB, userA);
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

        for (Future<Void> future :futures){
            future.get();
        }

        System.out.println("end following phase thread : " + Thread.currentThread().getName());
    }

    public void addingPhase() throws ExecutionException, InterruptedException {

        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Future<Void>> futures = new ArrayList<>();

        Map<Integer, AtomicInteger> somme = new ConcurrentHashMap<>();

        for (int i = 0; i < nbThread; i++)
            somme.put(i, new AtomicInteger());

        List<Integer> powerLawArray = generateValues(nbUsers, nbUsers, 600, SCALEUSAGE);

        Collections.sort(powerLawArray);

        MyCallableWithArgument myCallable = (Integer i) -> {

            Key user;

            somme.get(i%nbThread).addAndGet(powerLawArray.get(i));

            user = mapIndiceToKey.get(i);
            addOriginalUser(user);
            localUsersUsageProbability.get(i%nbThread).put(somme.get(i%nbThread).longValue(), user);
            localUsersUsageProbabilityRange.put(i%nbThread, localUsersUsageProbabilityRange.get(i%nbThread) + somme.get(i%nbThread).longValue());

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

            for (Key userA : mapIndiceToKey.values()){
                line += mapKeyToIndice.get(userA).toString();

                for (Key userB: map.get(userA)){
                    line += " " + mapKeyToIndice.get(userB);
                }

                line += "\n";
            }

            writer.write(line);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void loadGraph() throws InterruptedException, ClassNotFoundException {

        Set<Key> localSetUser = new HashSet<>();
        List<Integer> powerLawArray = generateValues(nbUsers, nbUsers, 1.3, SCALEUSAGE);
        Map<Key, Queue<Key>> tmpListUsersFollow = new HashMap<>();

        int val;

        for (int i = 0; i < nbUsers;) {
            Key user = generateUser();
            if (localSetUser.add(user)) {
                if (i % nbUsers * 0.05 == 0)
                    System.out.println(i);

                addOriginalUser(user);
                mapIndiceToKey.put(i, user);
                mapKeyToIndice.put(user,i);

                tmpListUsersFollow.put(user, new LinkedList<>());
                i++;
            }
        }

        String cheminFichier = "graph_following_retwis.txt";

        try {
            File fichier = new File(cheminFichier);

            FileReader fileReader = new FileReader(fichier);

            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line = bufferedReader.readLine();

            while (line != null){

                String[] values = line.split(" ");
                int userIndice = Integer.parseInt(values[0]);

                if (values.length <= 1){
                    for (int i = 0; i < 5; i++) {
                        val = random.get().nextInt(nbUsers);
                        followUser(mapIndiceToKey.get(userIndice), mapIndiceToKey.get(val));
                        tmpListUsersFollow.get(mapIndiceToKey.get(userIndice)).add(mapIndiceToKey.get(val));
                    }
                }else{
                    for (int j = 1; j < values.length; j++) {
                        followUser(mapIndiceToKey.get(userIndice), mapIndiceToKey.get(j));
                        tmpListUsersFollow.get(mapIndiceToKey.get(userIndice)).add(mapIndiceToKey.get(j));
                    }
                }

                line = bufferedReader.readLine();
            }

            bufferedReader.close();
            fileReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int nbLink;
        Map<Key, Integer> mapNbLinkPerUser = new HashMap<>();
        Map<Integer, AtomicInteger> sommeUsage = new HashMap<>();
        long sommeFollow = 0L;

        for (int i = 0; i < nbUsers; i++) {
            nbLink = 0;

            Key user = mapIndiceToKey.get(i);
            nbLink += mapFollowers.get(user).size();
            nbLink += mapFollowing.get(user).size();

            mapNbLinkPerUser.put(user, nbLink);
        }

        mapNbLinkPerUser = sortMapByValue(mapNbLinkPerUser);
        Collections.sort(powerLawArray);
        Collections.reverse(powerLawArray);

        for (int i = 0; i < nbThread; i++) {
            sommeUsage.put(i, new AtomicInteger());
        }

        int j = 0;

        for (Key user: mapNbLinkPerUser.keySet()){
            val = powerLawArray.get(j);
            sommeUsage.get(j%nbThread).addAndGet(val);
            sommeFollow += val;

            localUsersUsageProbability.get(j%nbThread).put(sommeUsage.get(j%nbThread).longValue(), user);
            localUsersUsageProbabilityRange.put(j%nbThread, sommeUsage.get(j%nbThread).longValue());
            usersFollowProbability.put(sommeFollow, user);
            listLocalUser.get(j%nbThread).add(user);
            listLocalUsersFollow.get(j%nbThread).put(user, tmpListUsersFollow.get(user));

            j++;
        }

        usersFollowProbabilityRange = sommeFollow;
    }

    private void loadCompleteGraph() throws ClassNotFoundException, InterruptedException {

        Set<Key> setUser = new HashSet<>();
        Map<Key, Queue<Key>> tmpListUsersFollow = new HashMap<>();

        for (int i = 0; i < nbThread;) {
            Key user = generateUser();
            if (setUser.add(user)){
                addOriginalUser(user);
                mapIndiceToKey.put(i, user);
                mapKeyToIndice.put(user,i);

                tmpListUsersFollow.put(user, new LinkedList<>());
                i++;
            }
        }

        for (int i = 0; i < nbThread; i++) {
            for (int j = 0; j < nbThread; j++) {

                if (i != j){
                    followUser(mapIndiceToKey.get(i), mapIndiceToKey.get(j));
                    tmpListUsersFollow.get(mapIndiceToKey.get(i)).add(mapIndiceToKey.get(j));
                }
            }
        }

        Map<Integer, AtomicInteger> sommeUsage = new HashMap<>();
        long sommeFollow = 0L;
        int j = 0;

        for (int i = 0; i < nbThread; i++) {
            sommeUsage.put(i, new AtomicInteger());
        }

        for (Key user: mapFollowing.keySet()){
            sommeUsage.get(j%nbThread).addAndGet(1);
            sommeFollow += 1;

            localUsersUsageProbability.get(j%nbThread).put(sommeUsage.get(j%nbThread).longValue(), user);
            localUsersUsageProbabilityRange.put(j%nbThread, sommeUsage.get(j%nbThread).longValue());
            usersFollowProbability.put(sommeFollow, user);
            listLocalUser.get(j%nbThread).add(user);
            listLocalUsersFollow.get(j%nbThread).put(user, tmpListUsersFollow.get(user));

            j++;
        }
        usersFollowProbabilityRange = sommeFollow;
    }


    private void loadDAPGraph() throws ClassNotFoundException, InterruptedException {

        Set<Key> setUser = new HashSet<>();
        Queue<Key> listUser = new LinkedList<>();
        Map<Key, Queue<Key>> tmpListUsersFollow = new HashMap<>();

        for (int i = 0; i < nbThread * 10;) {
            Key user = generateUser();
            if (setUser.add(user)){
                addOriginalUser(user);
                listUser.offer(user);
                mapIndiceToKey.put(i, user);
                mapKeyToIndice.put(user,i);

                tmpListUsersFollow.put(user, new LinkedList<>());
                i++;
            }
        }

        for (int i = 0; i < nbThread; i++) {
            for (int k = 0; k < 10; k++) {
                for (int j = 0; j < 10; j++) {
                    int v = j+(i*10);
                    int w = k+(i*10);
                    if (w != v){
                        followUser(mapIndiceToKey.get(w), mapIndiceToKey.get(v));
                        tmpListUsersFollow.get(mapIndiceToKey.get(w)).add(mapIndiceToKey.get(v));
                    }
                }
            }
        }

        Map<Integer, AtomicInteger> sommeUsage = new HashMap<>();
        long sommeFollow = 0L;
        int j = 1;

        for (int i = 0; i < nbThread*10; i++) {
            sommeUsage.put(i, new AtomicInteger());
        }

        int threadNum = 0;
        for (Key user: listUser){
            sommeUsage.get(threadNum).addAndGet(1);
            sommeFollow += 1;

            localUsersUsageProbability.get(threadNum).put(sommeUsage.get(threadNum).longValue(), user);
            localUsersUsageProbabilityRange.put(threadNum, sommeUsage.get(threadNum).longValue());
            usersFollowProbability.put(sommeFollow, user);
            listLocalUser.get(threadNum).add(user);
            listLocalUsersFollow.get(threadNum).put(user, tmpListUsersFollow.get(user));

            if (j%10 == 0)
                threadNum +=1;
//            System.out.println(threadNum);
            j++;
        }
/*
        for (int i = 0; i < nbThread; i++) {
            System.out.println("Thread : " + i + " contains : ");
            for (Key user : listLocalUsersFollow.get(i).keySet()){
                System.out.print(mapKeyToIndice.get(user) + " : ");
                for (Key user2 : listLocalUsersFollow.get(i).get(user)){
                    System.out.print(mapKeyToIndice.get(user2) + ", ");
                }
                System.out.println();
            }
            System.out.println();
            System.out.println();
        }*/
        usersFollowProbabilityRange = sommeFollow;
    }

    public static Map<Key, Integer> sortMapByValue(Map<Key, Integer> inputMap) {
        // Convert the inputMap to a List of Map.Entry objects
        List<Map.Entry<Key, Integer>> entryList = new ArrayList<>(inputMap.entrySet());

        // Sort the entryList using a custom comparator based on values
        Collections.sort(entryList, Comparator.comparing(Map.Entry::getValue));
        Collections.reverse(entryList);
        // Create a new LinkedHashMap to store the sorted entries
        Map<Key, Integer> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<Key, Integer> entry : entryList) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

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

    public void addOriginalUser(Key user) throws ClassNotFoundException {
        mapFollowing.put(user, new HashSet<>());
        mapTimelines.put(user, new Timeline(Factory.createQueue(typeQueue)));
        mapProfiles.put(user, 0);
        mapFollowers.put(user, new ConcurrentHashSet<>());

        System.out.println(user + " has been added");
        System.out.println(mapFollowers.keySet());
        System.out.println();
//        if (typeSet.contains("Extended"))
//            mapFollowers.put(user, Factory.createSet(typeSet, factoryIndice));
//        else
//            mapFollowers.put(user, Factory.createSet(typeSet, nbThread));
    }

    public void addUser(Key user, Set<Key> dummySet, Timeline<String> dummyTimeline, Profile dummyProfile) {
//        assert user != null : "User is null";
//        assert dummySet != null : "Set is null";
//        assert dummyTimeline != null : "Timeline is null";

        mapFollowers.put(user,dummySet);
        mapFollowing.put(user, dummySet);
        mapTimelines.put(user, dummyTimeline);
        mapProfiles.put(user, 0);
    }

    public void removeUser(Key user){
        System.out.println("REMOVE");
        mapFollowers.remove(user);
        mapFollowing.remove(user);
        mapTimelines.remove(user);
        mapProfiles.remove(user);
    }

    // Adding user_A to the followers of user_B
    // and user_B to the following of user_A
    // user_A  is following user_B
    public void followUser(Key userA, Key userB) throws InterruptedException {

        mapFollowers.get(userB)
                .add(userA);

        mapFollowing.get(userA)
                .add(userB);
    }

    // Removing user_A to the followers of user_B
    // and user_B to the following of user_A
    public void unfollowUser(Key userA, Key userB){
        mapFollowers.get(userB)
                .remove(userA);
        mapFollowing.get(userA)
                .remove(userB);
    }

    public void lightFollowUser(Key userA, Key userB){
        mapFollowing.get(userA).add(userB);
    }

    public void lightUnfollowUser(Key userA, Key userB){
        mapFollowing.get(userA).remove(userB);
    }

    public void tweet(Key user, String msg) throws InterruptedException {
        Set<Key> set = mapFollowers.get(user);

        for (Key follower : set) {
            Timeline<String> timeline = mapTimelines.get(follower);
            timeline.add(msg);
            break;
        }
    }

    public void showTimeline(Key user) throws InterruptedException {
        try{

            mapTimelines.get(user).read();
        }catch (NullPointerException e){
            System.out.println(user);
            System.out.println();
            System.out.println(mapTimelines);
            System.out.println(e);
            System.exit(0);
        }
    }

    public void updateProfile(Key user){
        mapProfiles.compute(user, (usr, profile) -> ++profile);
    }

    public void joinCommunity(Key user){
        community.add(user);
    }

    public void leaveCommunity(Key user){
        community.remove(user);
    }
}