package eu.cloudbutton.dobj;

import eu.cloudbutton.dobj.types.*;
import nl.peterbloem.powerlaws.Discrete;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.ExplicitBooleanOptionHandler;
import org.kohsuke.args4j.spi.StringArrayOptionHandler;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.kohsuke.args4j.OptionHandlerFilter.ALL;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class App {

    @Option(name="-set", required = true, usage = "type of Set")
    private String typeSet;

    @Option(name="-list", required = true, usage = "type of List")
    private String typeList;

    @Option(name="-counter", required = true, usage = "type of Counter")
    private String typeCounter;

    @Option(name = "-ratios", required = true, handler = StringArrayOptionHandler.class, usage = "ratios")
    private String[] ratios;

    @Option(name = "-nbThreads", usage = "Number of threads")
    private int nbThreads = Runtime.getRuntime().availableProcessors() / 2;

    @Option(name = "-nbTest", usage = "Number of test")
    private int nbTest = 1;

    @Option(name = "-time", usage = "test time (seconds)")
    private int time = 300;

    @Option(name = "-wTime", usage = "warming time (seconds)")
    private int wTime = 0;

    @Option(name = "-s", handler = ExplicitBooleanOptionHandler.class, usage = "Save the result")
    private boolean _s = false;

    private static AtomicBoolean flag;
    private static Map<String, AbstractSet<String>> follower;
    private static Map<String, AbstractCounter> nbFollower;
    private static Map<String, Timeline> timeline;

    private static DegradableCounter nbAdd;
    private static DegradableCounter nbFollow;
    private static DegradableCounter nbUnfollow;
    private static DegradableCounter nbTweet;
    private static DegradableCounter nbRead;

    private static AtomicLong timeAdd;
    private static AtomicLong timeFollow;
    private static AtomicLong timeUnfollow;
    private static AtomicLong timeTweet;
    private static AtomicLong timeRead;
    private static AtomicLong timeT;

    private static AtomicLong timeTotal;

    public static void main(String[] args) throws InterruptedException, IOException {
        new App().doMain(args);
    }

    public void doMain(String[] args) throws InterruptedException, IOException {
        CmdLineParser parser = new CmdLineParser(this);

        try{
            parser.parseArgument(args);

            if (args.length < 1)
                throw new CmdLineException(parser, "No argument is given");

            if (ratios.length != 4){
                throw new java.lang.Error("Number of ratios must be 4 (% add, % follow or unfollow, % tweet, % read)");
            }

            int total = 0;
            for (int ratio: Arrays.stream(ratios).mapToInt(Integer::parseInt).toArray()) {
                total += ratio;
            }

            if (total != 100){
                throw new java.lang.Error("Total ratio must be 100");
            }

        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            System.err.println("java SampleMain [options...] arguments...");
            // print the list of available options
            parser.printUsage(System.err);
            System.err.println();

            // print option sample. This is useful some time
            System.err.println("  Example: java eu.cloudbutton.dobj.App" + parser.printExample(ALL));

            return;
        }

        Factory factory = new Factory();
        String objectSet = "create" + typeSet;
        String objectList = "create" + typeList;
        String objectCounter = "create" + typeCounter;

        for (int i = 1; i <= nbThreads;) {
            for (int a = 1; a <= nbTest; a++) {
                System.out.println("test numero "+ a +" pour " + i + " thread(s)");
                java.util.List<Callable<Void>> callables = new ArrayList<>();
                ExecutorService executor = Executors.newFixedThreadPool(i);

                follower = new ConcurrentHashMap<>();
                nbFollower = new ConcurrentHashMap<>();
                timeline = new ConcurrentHashMap<>();

                nbAdd = new DegradableCounter();
                nbFollow = new DegradableCounter();
                nbUnfollow = new DegradableCounter();
                nbTweet = new DegradableCounter();
                nbRead = new DegradableCounter();

                timeAdd = new AtomicLong(0);
                timeFollow = new AtomicLong(0);
                timeUnfollow = new AtomicLong(0);
                timeTweet = new AtomicLong(0);
                timeRead = new AtomicLong(0);

                timeTotal = new AtomicLong(0);

                CountDownLatch latch = new CountDownLatch(i+1); // Additional count for the coordinator

                for (int j = 0; j < i; j++) {
                    RetwisApp retwisApp = new RetwisApp(
                            objectSet,
                            objectList,
                            objectCounter,
                            Arrays.stream(ratios).mapToInt(Integer::parseInt).toArray(),
                            latch, factory);
                    callables.add(retwisApp);
                }

                ExecutorService executorService = Executors.newFixedThreadPool(1);
                flag = new AtomicBoolean();
                flag.set(true);
                executorService.submit(new Coordinator(latch));

                List<Future<Void>> futures;
                futures = executor.invokeAll(callables);

                try{
                    for (Future<Void> future : futures) {
                        future.get();
                    }
                } catch (CancellationException | ExecutionException e) {
                    //ignore
                    System.out.println(e);
                }
                System.out.println("done computing");

                double nbtotalOP = nbAdd.read() + nbFollow.read() + nbUnfollow.read() + nbTweet.read() + nbRead.read();

                if (_s){
                    FileWriter fileWriter = new FileWriter("retwis_all_operations.txt", true);
                    PrintWriter printWriter = new PrintWriter(fileWriter);
                    printWriter.println(i +" "+ time/nbtotalOP);
                    printWriter.flush();

                    fileWriter = new FileWriter("retwis_add_operations.txt", true);
                    printWriter = new PrintWriter(fileWriter);
                    printWriter.println(i +" "+ ((double) timeAdd.get()/1_000_000_000)/(double)nbAdd.read());
                    printWriter.flush();

                    fileWriter = new FileWriter("retwis_follow_operations.txt", true);
                    printWriter = new PrintWriter(fileWriter);
                    printWriter.println(i +" "+ ((double)timeFollow.get()/1_000_000_000)/(double)nbFollow.read());
                    printWriter.flush();

                    fileWriter = new FileWriter("retwis_unfollow_operations.txt", true);
                    printWriter = new PrintWriter(fileWriter);
                    printWriter.println(i +" "+ ((double)timeUnfollow.get()/1_000_000_000)/(double)nbUnfollow.read());
                    printWriter.flush();

                    fileWriter = new FileWriter("retwis_tweet_operations.txt", true);
                    printWriter = new PrintWriter(fileWriter);
                    printWriter.println(i +" "+ ((double)timeTweet.get()/1_000_000_000)/(double)nbTweet.read());
                    printWriter.flush();

                    fileWriter = new FileWriter("retwis_read_operations.txt", true);
                    printWriter = new PrintWriter(fileWriter);
                    printWriter.println(i +" "+ ((double)timeRead.get()/1_000_000_000)/(double)nbRead.read());
                    printWriter.flush();
                }
//                System.out.println((double)(timeAdd.get()+timeFollow.get()+timeUnfollow.get()+timeTweet.get()+timeRead.get())/1_000_000_000 / i);
//                System.out.println((double) timeTotal.get()/1_000_000_000 / nbtotalOP);
                System.out.println(i +" "+ time/nbtotalOP);
                System.out.println("    -time/add : " + ((double) timeAdd.get()/1_000_000_000)/(double)nbAdd.read());
                System.out.println("    -time/follow : " + ((double)timeFollow.get()/1_000_000_000)/(double)nbFollow.read());
                System.out.println("    -time/unfollow : " + ((double)timeUnfollow.get()/1_000_000_000)/(double)nbUnfollow.read());
                System.out.println("    -time/tweet : " + ((double)timeTweet.get()/1_000_000_000)/(double)nbTweet.read());
                System.out.println("    -time/read : " + ((double)timeRead.get()/1_000_000_000)/(double)nbRead.read());

                TimeUnit.SECONDS.sleep(5);
                executor.shutdown();
            }


            i *= 2;
            if (i > nbThreads && i != 2 * nbThreads)
                i = nbThreads;
        }
        System.exit(0);
    }

    public static class RetwisApp implements Callable<Void>{

        protected static final int ITEM_PER_THREAD = 100;
        protected final ThreadLocalRandom random;
        private final String objectSet;
        private final String objectList;
        private final String objectCounter;
        private final int[] ratios;
        private final CountDownLatch latch;
        private final Factory factory;
        int add = 0;
        int follow = 0;
        int unfollow = 0;
        int tweet = 0;
        int read = 0;

        public RetwisApp(String objectSet, String objectList, String objectCounter, int[] ratios, CountDownLatch latch, Factory factory) {
            this.random = ThreadLocalRandom.current();
            this.objectSet = objectSet;
            this.objectList = objectList;
            this.objectCounter = objectCounter;
            this.ratios = ratios;
            this.latch = latch;
            this.factory = factory;
        }

        @Override
        public Void call(){

            int val;
            char type;
            long startTime, endTime, tAdd = 0, tFollow = 0, tUnfollow = 0, tTweet = 0, tRead = 0;

            try{

                fill_database();

                latch.countDown();

                latch.await();


                //warm up
                while (flag.get()){
                    val = random.nextInt(100);

                    if(val < ratios[0]){ // add
                        type = 'a';
                    }else if (val >= ratios[0] && val < ratios[0]+ratios[1]){ //follow or unfollow
                        if (val%2 == 0){ //follow
                            type = 'f';
                        }else{ //unfollow
                            type = 'u';
                        }
                    }else if (val >= ratios[0]+ratios[1] && val < ratios[0]+ratios[1]+ratios[2]){ //tweet
                        type = 't';
                    }else{ //read
                        type = 'r';
                    }

                    compute(type);
                }

                while (!flag.get()){

                    val = random.nextInt(100);

                    if(val < ratios[0]){ // add
                        type = 'a';
                    }else if (val >= ratios[0] && val < ratios[0]+ratios[1]){ //follow or unfollow
                        if (val%2 == 0){ //follow
                            type = 'f';
                        }else{ //unfollow
                            type = 'u';
                        }
                    }else if (val >= ratios[0]+ratios[1] && val < ratios[0]+ratios[1]+ratios[2]){ //tweet
                        type = 't';
                    }else{ //read
                        type = 'r';
                    }

                    startTime = System.nanoTime();
                    compute(type);
                    endTime = System.nanoTime();

                    switch (type){
                        case 'a':
                            add++;
                            tAdd += endTime - startTime;
                            break;
                        case 'f':
                            follow++;
                            tFollow += endTime - startTime;
                            break;
                        case 'u':
                            unfollow++;
                            tUnfollow += endTime - startTime;
                            break;
                        case 't':
                            tweet++;
                            tTweet += endTime - startTime;
                            break;
                        case 'r':
                            read++;
                            tRead += endTime - startTime;
                            break;
                    }
                }

                nbAdd.increment(add);
                nbFollow.increment(follow);
                nbUnfollow.increment(unfollow);
                nbTweet.increment(tweet);
                nbRead.increment(read);

                timeAdd.addAndGet(tAdd);
                timeFollow.addAndGet(tFollow);
                timeUnfollow.addAndGet(tUnfollow);
                timeTweet.addAndGet(tTweet);
                timeRead.addAndGet(tRead);

            } catch (InterruptedException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
            }
            return null;
        }

        public void dummyFunction() throws InterruptedException {
            TimeUnit.MICROSECONDS.sleep(10000);
        }

        public void fill_database() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {

            int n;
            String userA;
            String userB;

            //adding users
            for (int i = 0; i < ITEM_PER_THREAD/2; i++) {
                addUser("user_"+Thread.currentThread().getName()+"_"+i);
            }

            List<Integer> data = new Discrete(1, 1.35).generate(200);

            //Following phase
            for (int i = 0; i < ITEM_PER_THREAD/2; i++) {

                userA = "user_"+Thread.currentThread().getName()+"_"+i;
                int nbFollow = Math.min(data.get(random.nextInt(200)), follower.keySet().size());
//                System.out.println("nb follow du process '"+ userA +"' : " + nbFollow);

                for(int j = 0; j < nbFollow; j++){

                    n = random.nextInt(follower.keySet().size());

                    userB = (String) follower.keySet().toArray()[n];

                    follow(userA, userB);
                }
            }
        }

        public void compute(char type) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {

            int n = random.nextInt(ITEM_PER_THREAD);;
            String userA;
            String userB;

            switch (type){
                case 'a':
                    addUser("user_"+Thread.currentThread().getName()+"_"+n);
                break;
                case 'f':
                    userA = "user_"+Thread.currentThread().getName()+"_"+n;
                    n = random.nextInt(follower.keySet().size());
                    userB = (String) follower.keySet().toArray()[n];
                    follow(userA, userB);
                break;
                case 'u':
                    userA = "user_"+Thread.currentThread().getName()+"_"+n;
                    n = random.nextInt(follower.keySet().size());
                    userB = (String) follower.keySet().toArray()[n];
                    unfollow(userA, userB);
                break;
                case 't':
                    userA = "user_"+Thread.currentThread().getName()+"_"+n;
                    tweet(userA, "msg from " + userA);
                break;
                case 'r':
                    userA = "user_"+Thread.currentThread().getName()+"_"+n;
                    showTimeline(userA);
                break;
            }
        }

        public void addUser(String user) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
//            System.out.println("add");

            if(!follower.containsKey(user)) {
                follower.put(user, (AbstractSet) Factory.class.getDeclaredMethod(objectSet).invoke(factory));
                nbFollower.put(user, (AbstractCounter) Factory.class.getDeclaredMethod(objectCounter).invoke(factory));
                timeline.put(user, new Timeline((AbstractQueue) Factory.class.getDeclaredMethod(objectList).invoke(factory),
                                (AbstractCounter) Factory.class.getDeclaredMethod(objectCounter).invoke(factory))
                                                );
            }
        }

        public void follow(String userA, String userB){
//            System.out.println("follow");

            if(follower.keySet().contains(userA) && follower.keySet().contains(userB)) {
                follower.get(userB).add(userA);
                nbFollower.get(userB).increment();
            }
        }

        public void unfollow(String userA, String userB){
//            System.out.println("unfollow");

            if(follower.keySet().contains(userA) && follower.keySet().contains(userB)) {
                follower.get(userB).remove(userA);
                nbFollower.get(userB).write(-1);
            }
        }

        public void tweet(String user, String msg){
//            System.out.println("tweet");

            if(follower.keySet().contains(user)) {
                for (String u : follower.get(user)) {
                    timeline.get(u).add(msg);
                }
            }
        }

        public void showTimeline(String user){
//            System.out.println("Show timeline");
            if(follower.keySet().contains(user)) {
                timeline.get(user).read();
//                System.out.println(s);
            }
        }
    }

    public class Coordinator implements Callable<Void> {

        private final CountDownLatch latch;

        public Coordinator(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public Void call() throws Exception {
            try {
                latch.countDown();
                latch.await();
                System.out.println("warm up");
                TimeUnit.SECONDS.sleep(wTime);
                flag.set(false);
                System.out.println("computing");
                TimeUnit.SECONDS.sleep(time);
                flag.set(true);
            } catch (InterruptedException e) {
                throw new Exception("Thread interrupted", e);
            }
            return null;
        }
    }
}
