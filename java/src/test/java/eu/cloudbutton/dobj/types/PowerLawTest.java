package eu.cloudbutton.dobj.types;


import nl.peterbloem.powerlaws.DiscreteApproximate;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PowerLawTest {

    @Test
    void add() {
        List<Double> listAlpha = new ArrayList<>();

        for (double i = 1.315 ; i <= 1.315; i+=0.025) {
            listAlpha.add(i);
        }

        for (double alpha : listAlpha){
            List<Integer> data = new DiscreteApproximate(1, alpha).generate(1000);


            int i = 0, nbMax = 0, max = 600, avg = 0;


            for (int val: data){
                if (val >= max) {
                    nbMax++;
                    data.set(i,max);
                }
                if (val < 0)
                    data.set(i, 0);
                avg += data.get(i);
                i++;
            }

            Collections.sort(data);
            System.out.println(data);
            System.out.println();
            System.out.println("======= " + alpha + " =======");
            System.out.println("max : " + Collections.max(data) + " => " + Collections.max(data)/ 1000000.0 * 100 +"%");
            System.out.println("Q1 : " + data.get(data.size()/4) + " => " + data.get(data.size()/4) / 1000000.0 * 100 +"%");
            System.out.println("médiane : " + data.get(data.size()/2) + " => " + data.get(data.size()/2) / 1000000.0 * 100 +"%");
            System.out.println("Q3 : " + data.get(3*data.size()/4) + " => " + data.get(3*data.size()/4) / 1000000.0 * 100 +"%");
            System.out.println("nbMax : " + nbMax);
            System.out.println("avg : " + avg/data.size());
        }

    }

}
