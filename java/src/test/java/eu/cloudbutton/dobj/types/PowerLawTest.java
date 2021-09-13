package eu.cloudbutton.dobj.types;

import nl.peterbloem.powerlaws.Continuous;
import nl.peterbloem.powerlaws.Discrete;
import nl.peterbloem.powerlaws.DiscreteApproximate;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.List;
import java.util.concurrent.*;

public class PowerLawTest {


    private Factory factory;

    @BeforeTest
    void setUp() {
        factory = new Factory();
    }

    @Test
    void add() throws ExecutionException, InterruptedException {
        List<Integer> data = new DiscreteApproximate(1, 1.35).generate(1000);


//        System.out.println(data);
    }

}
