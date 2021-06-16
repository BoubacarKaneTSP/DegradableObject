import eu.cloudbutton.dobj.types.*;
import eu.cloudbutton.dobj.types.AbstractList;
import eu.cloudbutton.dobj.types.AbstractSet;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.StringArrayOptionHandler;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

import static org.kohsuke.args4j.OptionHandlerFilter.ALL;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class App {

    @Option(name="-set", usage = "type of set")
    private String typeSet;

    @Option(name="-list", usage = "type of List")
    private String typeList;

    @Option(name="-counter", usage = "type of Counter")
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

    private static boolean flag;
    private static final ConcurrentMap<String, AbstractSet<String>> users = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, Map<String, AbstractSet<String>>> follower = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, Map<String, AbstractCounter>> nbFollower = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, Map<String, AbstractList<String>>> timeline = new ConcurrentHashMap<>();

    public static void main(String[] args) throws InterruptedException {
        new App().doMain(args);
    }

    public void doMain(String[] args) throws InterruptedException {
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
            System.err.println("  Example: java App" + parser.printExample(ALL));

            return;
        }

        Factory factory = new Factory();
        String objectSet = "create" + typeSet;
        String objectList = "create" + typeList;
        String objectCounter = "create" + typeCounter;

        for (int i = 0; i <= nbThreads;) {
            for (int a = 0; a < nbTest; a++) {
                java.util.List<Callable<Void>> callables = new ArrayList<>();
                ExecutorService executor = Executors.newFixedThreadPool(i);

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
                flag = true;
                executorService.submit(new Coordinator());

                List<Future<Void>> futures;
                futures = executor.invokeAll(callables, time + wTime, TimeUnit.SECONDS);

                try{
                    for (Future<Void> future : futures) {
                        future.get();
                    }
                } catch (CancellationException | ExecutionException e) {
                    //ignore
                }
                TimeUnit.SECONDS.sleep(5);
                executor.shutdown();
            }


            i *= 2;
            if (i > nbThreads && i != 2 * nbThreads)
                i = nbThreads;
        }
    }

    public static class RetwisApp implements Callable<Void>{

        private final String objectSet;
        private final String objectList;
        private final String objectCounter;
        private final int[] ratios;
        private final CountDownLatch latch;
        private final Factory factory;
        private final String me = Thread.currentThread().getName();

        public RetwisApp(String objectSet, String objectList, String objectCounter, int[] ratios, CountDownLatch latch, Factory factory){
            this.objectSet = objectSet;
            this.objectList = objectList;
            this.objectCounter = objectCounter;
            this.ratios = ratios;
            this.latch = latch;
            this.factory = factory;
            follower.put(me, new HashMap<>());
            nbFollower.put(me, new HashMap<>());
            timeline.put(me, new HashMap<>());
        }

        @Override
        public Void call(){
            latch.countDown();
            Long i = 0L;

            try{
                latch.await();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        public void addUser(String user) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
            if(!users.get(me).contains(user)) {
                users.get(me).add(user);
                follower.get(me).put(user, (AbstractSet) Factory.class.getDeclaredMethod(objectSet).invoke(factory));
                nbFollower.get(me).put(user, (AbstractCounter) Factory.class.getDeclaredMethod(objectCounter).invoke(factory));
                timeline.get(me).put(user, (AbstractList) Factory.class.getDeclaredMethod(objectList).invoke(factory));
            }
        }

        public void follow(String userA, String userB){

            follower.get(me).get(userB).add(userA);
            nbFollower.get(me).get(userB).increment();
        }

        public void unfollow(String userA, String userB){

            follower.get(me).get(userB).remove(userA);
            nbFollower.get(me).get(userB).write(-1);
        }

        public void tweet(String user, String msg){

            for (String u : follower.get(me).get(user).read())
                timeline.get(me).get(u).append(msg);
        }

        public java.util.List<String> showTimeline(String user){
            return timeline.get(me).get(user).read();
        }
    }

    public class Coordinator implements Callable<Void> {
        @Override
        public Void call() throws Exception {
            try {
                TimeUnit.SECONDS.sleep(wTime);
                flag=false;
            } catch (InterruptedException e) {
                throw new Exception("Thread interrupted", e);
            }
            return null;
        }
    }
}
