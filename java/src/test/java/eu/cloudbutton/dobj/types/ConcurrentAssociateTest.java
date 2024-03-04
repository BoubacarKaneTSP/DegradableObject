package eu.cloudbutton.dobj.types;

import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ConcurrentAssociateTest {



    @Test
    public void testPut() {
        System.out.println("do nothing");
//        test("CHM.put", (m, o) -> m.put(o, o));
    }


    static void dumpTestThreads() {
        System.out.println("dump test thread");
    }
}
