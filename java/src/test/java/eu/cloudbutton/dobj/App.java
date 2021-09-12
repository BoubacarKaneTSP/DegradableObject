package eu.cloudbutton.dobj;

import eu.cloudbutton.dobj.types.*;
import nl.peterbloem.powerlaws.Discrete;
import nl.peterbloem.powerlaws.DiscreteApproximate;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.StringArrayOptionHandler;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.AbstractList;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

import static org.kohsuke.args4j.OptionHandlerFilter.ALL;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

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

    @Option(name = "-time", usage = "How long will the test last (seconds)")
    private int time = 300;

    @Option(name = "-wTime", usage = "How long we wait till the test start (seconds)")
    private int wTime = 0;

    private static AtomicBoolean flag;
    private static Map<String, AbstractSet<String>> follower;
    private static Map<String, AbstractCounter> nbFollower;
    private static Map<String, AbstractQueue<String>> timeline;

    private static Map<Thread, Integer> nbAdd;
    private static Map<Thread, Integer> nbFollow;
    private static Map<Thread, Integer> nbUnfollow;
    private static Map<Thread, Integer> nbTweet;
    private static Map<Thread, Integer> nbRead;

    public static void main(String[] args) throws InterruptedException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        new App().doMain(args);
    }

    public void doMain(String[] args) throws InterruptedException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        CmdLineParser parser = new CmdLineParser(this);

        try{
            // parse the arguments.
            parser.parseArgument(args);

            // you can parse additional arguments if you want.
            // parser.parseArgument("more","args");

            // after parsing arguments, you should check
            // if enough arguments are given.
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
            // if there's a problem in the command line,
            // you'll get this exception. this will report
            // an error message.
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
            for (int a = 0; a < nbTest; a++) {
                System.out.println("test numero "+ a +" pour " + i + " thread(s)");
                java.util.List<Callable<Void>> callables = new ArrayList<>();
                ExecutorService executor = Executors.newFixedThreadPool(i);

                follower = new ConcurrentHashMap<>();
                nbFollower = new ConcurrentHashMap<>();
                timeline = new ConcurrentHashMap<>();

                nbAdd = new ConcurrentHashMap<>();
                nbFollow = new ConcurrentHashMap<>();
                nbUnfollow = new ConcurrentHashMap<>();
                nbTweet = new ConcurrentHashMap<>();
                nbRead = new ConcurrentHashMap<>();

                CountDownLatch latch = new CountDownLatch(i);

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

                int totalAdd = 0, totalFollow = 0, totalUnfollow = 0, totalTweet = 0, totalRead = 0;

                for(int n : nbAdd.values())
                    totalAdd += n;

                for(int n : nbFollow.values())
                    totalFollow += n;

                for(int n : nbUnfollow.values())
                    totalUnfollow += n;

                for(int n : nbTweet.values())
                    totalTweet += n;

                for(int n : nbRead.values())
                    totalRead += n;



                System.out.println("nb add : "+ totalAdd);
                System.out.println("nb Follow : "+ totalFollow);
                System.out.println("nb Unfollow : "+ totalUnfollow);
                System.out.println("nb Tweet : "+ totalTweet);
                System.out.println("nb Read : "+ totalRead);
                TimeUnit.SECONDS.sleep(10);
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

        public RetwisApp(String objectSet, String objectList, String objectCounter, int[] ratios, CountDownLatch latch, Factory factory) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
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

            System.out.println("ratios : " + ratios[0]);
            System.out.println("ratios : " + ratios[1]);
            System.out.println("ratios : " + ratios[2]);

            System.out.println(ratios[0]+ratios[1]);
            System.out.println(ratios[0]+ratios[1]+ratios[2]);
            try{

                fill_database();

                latch.countDown();

                latch.await();

//                System.out.println(follower.keySet().size());

                //warm up
                while (flag.get()){
                    compute();
                }

                while (!flag.get()){
                    compute();
                }

            } catch (InterruptedException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
            }
            return null;
        }

        public void fill_database() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {

            int n;
            String userA;
            String userB = null;

            //adding users
            for (int i = 0; i < ITEM_PER_THREAD/2; i++) {
                addUser("user_"+Thread.currentThread().getName()+"_"+i);
            }

            List<Integer> data = new Discrete(1, 1.35).generate(200);
            System.out.println(data);
            int max = data.get(0);
            int min = data.get(0);
            for(int i : data){
                if (i>= max)
                    max = i;
            }
            for(int i : data){
                if (i<= min)
                    min = i;
            }
            System.out.println("nb follow max : " + max);
            System.out.println("nb follow min : " + min);
            //Following phase
            for (int i = 0; i < ITEM_PER_THREAD/2; i++) {

                userA = "user_"+Thread.currentThread().getName()+"_"+i;
                int nbFollow = Math.min(data.get(random.nextInt(200)), follower.keySet().size());
                System.out.println("nb follow du process '"+ userA +"' : " + nbFollow);

                for(int j = 0; j < nbFollow; j++){

                    n = random.nextInt(follower.keySet().size());

                    userB = (String) follower.keySet().toArray()[n];

                    follow(userA, userB);
                }
            }
        }

        public void compute() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {

            int n = random.nextInt(ITEM_PER_THREAD);
            int val = n%100;
            String userA;
            String userB = null;

            if(val < ratios[0]){
                //add
                addUser("user_"+Thread.currentThread().getName()+"_"+n);
                add++;
            }else if (val >= ratios[0] && val < ratios[0]+ratios[1]){
                //follow or unfollow
                userA = "user_"+Thread.currentThread().getName()+"_"+n;
                n = random.nextInt(follower.keySet().size());

                userB = (String) follower.keySet().toArray()[n];

                if (val%2 == 0){
                    follow(userA, userB);
                    follow++;
//                    System.out.println(follower.get(userB).read());
                }else{
                    unfollow(userA, userB);
                    unfollow++;
                }
            }else if (val >= ratios[0]+ratios[1] && val < ratios[0]+ratios[1]+ratios[2]){
                //tweet
                userA = "user_"+Thread.currentThread().getName()+"_"+n;
                tweet(userA, "msg from " + userA);
                tweet++;
            }else{
                //read
                userA = "user_"+Thread.currentThread().getName()+"_"+n;
                showTimeline(userA);
                read++;
            }

            nbAdd.put(Thread.currentThread(), add);
            nbFollow.put(Thread.currentThread(), follow);
            nbUnfollow.put(Thread.currentThread(), unfollow);
            nbTweet.put(Thread.currentThread(), tweet);
            nbRead.put(Thread.currentThread(), read);
        }

        public void addUser(String user) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
//            System.out.println("add");

            if(!follower.keySet().contains(user)) {
                follower.put(user, (AbstractSet) Factory.class.getDeclaredMethod(objectSet).invoke(factory));
                nbFollower.put(user, (AbstractCounter) Factory.class.getDeclaredMethod(objectCounter).invoke(factory));
                timeline.put(user, (AbstractQueue) Factory.class.getDeclaredMethod(objectList).invoke(factory));
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
//                    System.out.println("twit");
                    timeline.get(u).add(msg);
                }
            }
        }

        public void showTimeline(String user){
//            System.out.println("Show timeline");

           /* for(AbstractList<String> l : timeline.values()){
                System.out.println(l.read());
            }
            */
            if(follower.keySet().contains(user)) {
//                if(timeline.get(user).read().size() > 0)
//                    System.out.println(timeline.get(user).read());
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
                latch.await();
                System.out.println("warm up");
                TimeUnit.SECONDS.sleep(wTime);
                flag.set(false);
                System.out.println("computing");
                TimeUnit.SECONDS.sleep(time);
                flag.set(true);
                System.out.println("done");
            } catch (InterruptedException e) {
                throw new Exception("Thread interrupted", e);
            }
            return null;
        }
    }
}
