package eu.cloudbutton.dobj.types;

import org.javatuples.Pair;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.testng.Assert.*;


public class ClonerTest {

    private ClonerPairCounterInt<Counter> cloner ;

    @BeforeTest
    void setUp() {
        cloner = new ClonerPairCounterInt<>();
    }

    @Test
    void test(){

        Pair<Counter, Integer> PCI = new Pair<>(new Counter(3), 10);
        Pair<Counter,Integer> PCI2 = cloner.clone(PCI);
        System.out.println(PCI);
        System.out.println(PCI2);
        assertEquals(PCI.getValue0().read(),PCI2.getValue0().read(),"Failed cloning the Counter Part in Pair");
        assertEquals(PCI.getValue1(),PCI2.getValue1(),"Failed cloning the Int Part Pair");

        PCI = PCI.setAt1(1);
        PCI = PCI.setAt0(new Counter(9));
        assertNotEquals(PCI.getValue0().read(),PCI2.getValue0().read(),"Failed to create a new reference for the Counter Part in Pair");
        assertNotEquals(PCI.getValue1(),PCI2.getValue1(),"Failed to create a new reference for the Int Part Pair");
    }


}
