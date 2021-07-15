package eu.cloudbutton.dobj.types;

import org.javatuples.Pair;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;


import java.util.concurrent.atomic.AtomicInteger;

import static org.testng.Assert.*;


public class ClonerTest {

    private ClonerPairCounterInt<Counter> cloner ;

//    @BeforeTest
//    void setUp() {
//        cloner = new ClonerPairCounterInt<>();
//    }

    @Test
    void test(){/*
        AtomicInteger c = new AtomicInteger(10);
        Pair<AtomicInteger, Integer> PCI = new Pair<>(c, 10);
        *//*
        Pair<Counter,Integer> PCI2 = cloner.clone(PCI);
        System.out.println(PCI);
        System.out.println(PCI2);
        assertEquals(PCI.getValue0().read(),PCI2.getValue0().read(),"Failed cloning the Counter Part in Pair");
        assertEquals(PCI.getValue1(),PCI2.getValue1(),"Failed cloning the Int Part Pair");
*//*

        c.incrementAndGet();
        Pair<AtomicInteger,Integer> PCI3 =  PCI.setAt0(c);
        assertNotEquals(PCI.getValue0().get(),PCI3.getValue0().get(),"Failed to create a new reference for the Counter Part in Pair");
//        assertNotEquals(PCI.getValue1(),PCI3.getValue1(),"Failed to create a new reference for the Int Part Pair");
    */}


}
