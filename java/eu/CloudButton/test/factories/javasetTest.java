package factories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class javasetTest {

    private javaset set;

    @BeforeEach
    public void setUp(){

        setfactory setFactory = new javasetfactory();
        set = null;
        set = setFactory.getjavaset();
    }

    @DisplayName("Ensure adding is correct")
    @Test
    public void add() {
        set.write("value1");
        set.write("value2");

        Set<String> s = new HashSet<>();
        s.add("value1");
        s.add("value2");

        assertEquals(s, set.read(), "Failed adding an element to a set");
    }

    @DisplayName("Ensure removing is correct")
    @Test
    public void remove() {
        set.write("value1");
        set.write("value2");
        set.remove("value2");

        Set<String> s = new HashSet<>();
        s.add("value1");

        assertEquals(s, set.read(), "Failed removing an element to a set");
    }

    @DisplayName("Analyzing performances with one thread")
    @Test
    public void performancesolo(){
        final int nbOp = 10000000;

        for (int i = 0; i < nbOp; i++) {
            set.write(Double.toString(Math.random()));
        }

        assertEquals(nbOp, set.read().size(), "Missing entries");
    }
    static class testCallable implements Callable<javaset>{
        private final javaset set;

        public testCallable(javaset set) {
            this.set = set;
        }


        @Override
        public javaset call() {
            String pid_name = Thread.currentThread().getName();
            int nbOp = 10;
            for (int i = 0; i < nbOp; i++) {
                System.out.println("my_name : "+pid_name+ ", i = "+i+ ", nbOp = " + nbOp);
                this.set.write(String.valueOf(i));
            }
            return this.set;
        }
    }
    @DisplayName("Analyzing performances with multiple thread")
    @Test
    public void performancemulti() {
        final int nbThreads = 10;

        ExecutorService executor = Executors.newFixedThreadPool(nbThreads);

        for (int i = 0; i < nbThreads; i++) {
            executor.submit(new testCallable(set));
        }

        System.out.println(set.read());

    }
}