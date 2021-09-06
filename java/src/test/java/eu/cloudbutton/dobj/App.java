package eu.cloudbutton.dobj;

import eu.cloudbutton.dobj.types.*;
import eu.cloudbutton.dobj.types.AbstractList;
import eu.cloudbutton.dobj.types.AbstractSet;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.StringArrayOptionHandler;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
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
    private static Set<String> users;
    private static Map<String, AbstractSet<String>> follower;
    private static Map<String, AbstractCounter> nbFollower;
    private static Map<String, AbstractList<String>> timeline;

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

                users = new ConcurrentSkipListSet<>();
                follower = new ConcurrentHashMap<>();
                nbFollower = new ConcurrentHashMap<>();
                timeline = new ConcurrentHashMap<>();

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
                executorService.submit(new Coordinator());

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

        protected static final int ITEM_PER_THREAD = 20;
        protected final ThreadLocalRandom random;
        private final String objectSet;
        private final String objectList;
        private final String objectCounter;
        private final int[] ratios;
        private final CountDownLatch latch;
        private final Factory factory;

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

            latch.countDown();

            try{
                latch.await();

                for (int j = 0; j < ITEM_PER_THREAD/2; j++) {
                    addUser("user_"+Thread.currentThread().getName()+"_"+j);
                }
                System.out.println(users.size());
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

        public void compute() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {

            int n = random.nextInt(ITEM_PER_THREAD);
            int val = n%100;
            String userA;
            String userB = null;

            if(val < ratios[0]){
                //add
//                System.out.println("add");
                addUser("user_"+Thread.currentThread().getName()+"_"+n);

            }else if (val >= ratios[0] && val < ratios[0]+ratios[1]){
                //follow or unfollow
//                System.out.println("follow or unfollow");
                userA = "user_"+Thread.currentThread().getName()+"_"+n;
                n = random.nextInt(users.size());
                int i = 0;
                for(String usr : users){
                    if (i == n){
                        userB = usr;
                        break;
                    }
                    i++;
                }

                if (val%2 == 0){
                    follow(userA, userB);
//                    System.out.println(follower.get(userB).read());
                }else{
                    unfollow(userA, userB);
                }
            }else if (val >= ratios[0]+ratios[1] && val < ratios[0]+ratios[1]+ratios[2]){
                //tweet
//                System.out.println("tweet");
                userA = "user_"+Thread.currentThread().getName()+"_"+n;
                tweet(userA, "msg from " + userA);
            }else{
                //read
//                System.out.println("Show timeline");
                userA = "user_"+Thread.currentThread().getName()+"_"+n;
                showTimeline(userA);
            }
        }

        public void addUser(String user) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
            if(!users.contains(user)) {
                follower.put(user, (AbstractSet) Factory.class.getDeclaredMethod(objectSet).invoke(factory));
                nbFollower.put(user, (AbstractCounter) Factory.class.getDeclaredMethod(objectCounter).invoke(factory));
                timeline.put(user, (AbstractList) Factory.class.getDeclaredMethod(objectList).invoke(factory));
                users.add(user);
            }
        }

        public void follow(String userA, String userB){
            if(users.contains(userA)) {
                follower.get(userB).add(userA);
                nbFollower.get(userB).increment();
            }
        }

        public void unfollow(String userA, String userB){
            if(users.contains(userA)) {
                follower.get(userB).remove(userA);
                nbFollower.get(userB).write(-1);
            }
        }

        public void tweet(String user, String msg){
            if(users.contains(user)) {
                for (String u : follower.get(user).read()) {
//                    System.out.println("twit");
                    timeline.get(u).append(msg);
                }
            }
        }

        public void showTimeline(String user){
           /* for(AbstractList<String> l : timeline.values()){
                System.out.println(l.read());
            }
            */
            if(users.contains(user)) {
//                if(timeline.get(user).read().size() > 0)
                    System.out.println(timeline.get(user).read());
            }
        }
    }

    public class Coordinator implements Callable<Void> {
        @Override
        public Void call() throws Exception {
            try {
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
