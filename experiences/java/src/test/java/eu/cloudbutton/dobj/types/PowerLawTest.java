package eu.cloudbutton.dobj.types;


import nl.peterbloem.powerlaws.Discrete;
import nl.peterbloem.powerlaws.DiscreteApproximate;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PowerLawTest {


    private Factory factory;

    @BeforeTest
    void setUp() {
        factory = new Factory();
    }

    @Test
    void add() {
        List<Double> listAlpha = new ArrayList<>();

        for (double i = 1.15 ; i < 1.35; i+=0.025) {
            listAlpha.add(i);
        }

        for (double alpha : listAlpha){
            List<Integer> data = new DiscreteApproximate(1, alpha).generate(1000);


            int i = 0;
            int nbMax = 0;

            for (int val: data){
                if (val >= 16000)
                    nbMax++;
                if (val < 0)
                    data.set(i, 0);
                i++;
            }
            Collections.sort(data);
            System.out.println(data);
            System.out.println();
            System.out.println("======= " + alpha + " =======");
            System.out.println("max : " + Collections.max(data));
            System.out.println("Q1 : " + data.get(data.size()/4) + " => " + data.get(data.size()/4) / 16000.0 * 100 +"%");
            System.out.println("médiane : " + data.get(data.size()/2) + " => " + data.get(data.size()/2) / 16000.0 * 100 +"%");
            System.out.println("Q3 : " + data.get(3*data.size()/4) + " => " + data.get(3*data.size()/4) / 16000.0 * 100 +"%");
            System.out.println("nbMax : " + nbMax);
        }

    }

}
