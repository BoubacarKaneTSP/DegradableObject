package eu.cloudbutton.dobj.Benchmark;

import eu.cloudbutton.dobj.Benchmark.Tester.FactoryFiller;
import eu.cloudbutton.dobj.Benchmark.Tester.FactoryTester;
import eu.cloudbutton.dobj.Benchmark.Tester.Filler;
import eu.cloudbutton.dobj.Benchmark.Tester.Tester;
import eu.cloudbutton.dobj.types.*;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.ExplicitBooleanOptionHandler;
import org.kohsuke.args4j.spi.StringArrayOptionHandler;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.kohsuke.args4j.OptionHandlerFilter.ALL;

public class Benchmark {

    public static AtomicLong timeAdd;
    public static AtomicLong timeRemove;
    public static AtomicLong timeRead;
    public static AtomicInteger nbAdd;
    public static AtomicInteger nbRemove;
    public static AtomicInteger nbRead;
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
    @Option(name = "-s", handler = ExplicitBooleanOptionHandler.class, usage = "Save the result")
    private boolean _s = false;
    @Option(name = "-p", handler = ExplicitBooleanOptionHandler.class, usage = "Print the result")
    private boolean _p = false;

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

            PrintWriter printWriter = null;
            FileWriter fileWriter;

            Factory factory = new Factory();
            String constructor = "create" + type;

            for (int nbCurrentThread = 1; nbCurrentThread <= nbThreads; ) {
                nbAdd = new AtomicInteger(0);
                nbRemove = new AtomicInteger(0);
                nbRead = new AtomicInteger(0);

                timeAdd = new AtomicLong(0);
                timeRemove = new AtomicLong(0);
                timeRead = new AtomicLong(0);
                for (int a = 0; a < nbTest; a++) {

                    Class clazz;
                    try{
                        clazz = Class.forName("eu.cloudbutton.dobj.types."+type);
                    }catch (ClassNotFoundException e){
                        try{
                            clazz = Class.forName("java.util.concurrent."+type);
                        }catch (ClassNotFoundException classNotFoundException){
                            clazz = Class.forName("java.util.concurrent.atomic."+type);
                        }
                    }

                    Object object;
                    try{
                        object = Factory.class.getDeclaredMethod(constructor).invoke(factory);
                    }catch (NoSuchMethodException e){
                        object = clazz.getConstructor().newInstance();
                    }




                    FactoryFiller factoryFiller = new FactoryFiller(object, 100);

                    try{
                        Method method = factoryFiller.getClass().getDeclaredMethod("create"+ clazz.getSuperclass().getSimpleName() + "Filler");
                        Filler filler = (Filler) method.invoke(factoryFiller);
                        filler.fill();
                    }catch (NoSuchMethodException e){
                        System.out.println(clazz.getSuperclass().getSimpleName() + " object may not need to be filled");
                    }


                    List<Callable<Void>> callables = new ArrayList<>();
                    ExecutorService executor = Executors.newFixedThreadPool(nbCurrentThread);

                    CountDownLatch latch = new CountDownLatch(nbCurrentThread);
                    FactoryTester factoryTester = new FactoryTester(
                            object,
                            Arrays.stream(ratios).mapToInt(Integer::parseInt).toArray(), //new int[] {100},
                            latch
                    );

                    for (int j = 0; j < nbCurrentThread; j++) {
                        Method m = factoryTester.getClass().getDeclaredMethod("create" + clazz.getSuperclass().getSimpleName() + "Tester");
                        Tester tester = (Tester) m.invoke(factoryTester);
                        callables.add(tester);
                    }


                   /*

                   Code if one reader is needed

                   FactoryTester factoryT = new FactoryTester(
                            object,
                            Arrays.stream(ratios).mapToInt(Integer::parseInt).toArray(),
                            latch,
                            nbOps/nbCurrentThread
                    );

                    Method m1 = factoryT.getClass().getDeclaredMethod("create" + clazz.getSuperclass().getSimpleName() + "Tester");
                    Tester t = (Tester) m1.invoke(factoryT);
                    callables.add(t);
*/
                    ExecutorService executorCoordinator = Executors.newFixedThreadPool(1);
                    flag = new AtomicBoolean();
                    flag.set(true);
                    executorCoordinator.submit(new Coordinator());

                    List<Future<Void>> futures;

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
                long avgTimeTotal;
                avgTimeTotal = (timeAdd.get() + timeRemove.get() + timeRead.get()) / nbTest;

                sum = (nbAdd.get() + nbRemove.get() + nbRead.get()) / nbTest ;

                if (_s){

                    if (nbCurrentThread == 1)
                        fileWriter = new FileWriter("results_"+type+"_ratio_write_"+ratios[0]+".txt", false);
                    else
                        fileWriter = new FileWriter("results_"+type+"_ratio_write_"+ratios[0]+".txt", true);

                    printWriter = new PrintWriter(fileWriter);
                    printWriter.println(nbCurrentThread + " " + (double)sum / (avgTimeTotal/1_000_000_000));
                }

                if (_p){
                    System.out.println(nbCurrentThread + " " + (double)sum / (avgTimeTotal/1_000_000_000)); // printing the throughput per op for nbCurrentThread thread(s)
                    System.out.println("    -time/add : " + (double)nbAdd.get() / (timeAdd.get() /1_000_000_000));
                    System.out.println("    -time/remove : " + (double)nbRemove.get() / (timeRemove.get() /1_000_000_000));
                    System.out.println("    -time/read: " + (double)nbRead.get() / (timeRead.get() /1_000_000_000));
                }

                nbCurrentThread *= 2;

                /*if(nbCurrentThread==2)
                    nbCurrentThread = nbThreads;
*/
                if (nbCurrentThread > nbThreads && nbCurrentThread != 2 * nbThreads) {
                    nbCurrentThread = nbThreads;
                }

                if(_p)
                    System.out.println();
                if (_s)
                    printWriter.close();
            }

        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | ClassNotFoundException | InstantiationException | IOException e) {
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
