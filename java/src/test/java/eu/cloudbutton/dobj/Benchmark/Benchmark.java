package eu.cloudbutton.dobj.Benchmark;

import eu.cloudbutton.dobj.Benchmark.Tester.FactoryFiller;
import eu.cloudbutton.dobj.Benchmark.Tester.FactoryTester;
import eu.cloudbutton.dobj.Benchmark.Tester.Filler;
import eu.cloudbutton.dobj.Benchmark.Tester.Tester;
import eu.cloudbutton.dobj.types.*;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.StringArrayOptionHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static org.kohsuke.args4j.OptionHandlerFilter.ALL;

public class Benchmark {

    public static AtomicLong timeAdd;
    public static AtomicLong timeRemove;
    public static AtomicLong timeRead;
    public static DegradableCounter nbAdd;
    public static DegradableCounter nbRemove;
    public static DegradableCounter nbRead;
    public static AtomicBoolean flag;

    @Option(name = "-type", required = true, usage = "type to test")
    private String type;
    @Option(name = "-ratios", handler = StringArrayOptionHandler.class, usage = "ratios")
    private String[] ratios;
    @Option(name = "-nbThreads", usage = "Number of threads")
    private int nbThreads = Runtime.getRuntime().availableProcessors() / 2;
    @Option(name = "-time", usage = "How long will the test last (seconds)")
    private int time = 300;
    @Option(name = "-wTime", usage = "How long we wait till the test start (seconds)")
    private int wTime = 0;
    @Option(name = "-nbOps", usage = "Number of operations between two time's read")
    private long nbOps = 100_000_000;
    @Option(name = "-nbTest", usage = "Number of test")
    private int nbTest = 1;

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        new Benchmark().doMain(args);
    }

    public void doMain(String[] args) throws InterruptedException, ExecutionException {
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
            System.err.println("  Example: java Benchmark" + parser.printExample(ALL));

            return;
        }

        try{

            Factory factory = new Factory();
            String constructor = "create" + type;

            for (int i = 1; i <= nbThreads; ) {
                for (int a = 0; a < nbTest; a++) {

                    nbAdd = new DegradableCounter();
                    nbRemove = new DegradableCounter();
                    nbRead = new DegradableCounter();

                    timeAdd = new AtomicLong(0);
                    timeRemove = new AtomicLong(0);
                    timeRead = new AtomicLong(0);

                    Object object = Factory.class.getDeclaredMethod(constructor).invoke(factory);
                    Class clazz;
                    try{
                        clazz = Class.forName("eu.cloudbutton.dobj.types."+type);
                    }catch (ClassNotFoundException e){
                        clazz = Class.forName("java.util.concurrent."+type);
                    }

                    FactoryFiller factoryFiller = new FactoryFiller(this, object, 1_000_000);

                    Method method = factoryFiller.getClass().getDeclaredMethod("create"+ clazz.getSuperclass().getSimpleName() + "Filler");
                    Filler filler = (Filler) method.invoke(factoryFiller);
                    filler.fill();

                    List<Callable<Void>> callables = new ArrayList<>();
                    ExecutorService executor = Executors.newFixedThreadPool(i);

                    CountDownLatch latch = new CountDownLatch(i);
                    FactoryTester factoryTester = new FactoryTester(this,
                            object,
                            Arrays.stream(ratios).mapToInt(Integer::parseInt).toArray(), //new int[] {100},
                            latch,
                            nbOps / i);
                    for (int j = 0; j < i-1; j++) {
                        Method m = factoryTester.getClass().getDeclaredMethod("create" + clazz.getSuperclass().getSimpleName() + "Tester");
                        Tester tester = (Tester) m.invoke(factoryTester);
                        callables.add(tester);
                    }


                    FactoryTester factoryT = new FactoryTester(this,
                            object,
                            Arrays.stream(ratios).mapToInt(Integer::parseInt).toArray(),
                            latch,
                            nbOps/i
                    );

                    Method m1 = factoryT.getClass().getDeclaredMethod("create" + clazz.getSuperclass().getSimpleName() + "Tester");
                    Tester t = (Tester) m1.invoke(factoryT);
                    callables.add(t);

                    ExecutorService executorService = Executors.newFixedThreadPool(1);
                    flag = new AtomicBoolean();
                    flag.set(true);
                    executorService.submit(new Coordinator());

                    List<Future<Void>> futures;

//                    System.out.println("Max JVM memory: " + Runtime.getRuntime().maxMemory());

                    // launch computation
                    futures = executor.invokeAll(callables);
                    try{
                        for (Future<Void> future : futures) {
                            future.get();
                        }
                    } catch (CancellationException e) {
                        //ignore
                        System.out.println(e);
                    }

                    executor.shutdownNow();
                    TimeUnit.SECONDS.sleep(1);
                }
                int sum;
                long timeTotal;
                timeTotal = timeAdd.get() + timeRemove.get() + timeRead.get();

                sum = nbAdd.read() + nbRemove.read() + nbRead.read();
                double avg_op = sum / i;
                System.out.println(i + " " + (timeTotal/1_000_000_000) / (double) sum); // printing the avg time per op for i thread(s)
                System.out.println("    -time/add : " + ((double) timeAdd.get()/1_000_000_000)/(double)nbAdd.read());
                System.out.println("    -time/remove : " + ((double)timeRemove.get()/1_000_000_000)/(double)nbRemove.read());
                System.out.println("    -time/read: " + ((double)timeRead.get()/1_000_000_000)/(double)nbRead.read());

                i *= 2;

                /*if(i==2)
                    i = nbThreads;
*/
                if (i > nbThreads && i != 2 * nbThreads) {
                    i = nbThreads;
                }
            }

        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        System.exit(0);
    }


    public class Coordinator implements Callable<Void> {

        public Coordinator(){}

        @Override
        public Void call() throws Exception {
            try {
                TimeUnit.SECONDS.sleep(wTime);
                flag.set(false);
                TimeUnit.SECONDS.sleep(time);
                flag.set(true);


            } catch (InterruptedException e) {
                throw new Exception("Thread interrupted", e);
            }
            return null;
        }
    }
}