package eu.cloudbutton.dobj.types;


import nl.peterbloem.powerlaws.DiscreteApproximate;
import org.testng.annotations.Test;

import java.util.*;

public class PowerLawTest {

    @Test
    void add() {
        List<Double> listAlpha = new ArrayList<>();

        /*for (double i = 1.315 ; i <= 1.315; i+=0.025) {
            listAlpha.add(i);
        }*/

        listAlpha.add(1.39);

        for (double alpha : listAlpha){
            List<Integer> data = new DiscreteApproximate(1, alpha).generate(4);

            int nbUsers = 1000000;
            double ratio = 100000 / 175000000.0; //10⁵ is ~ the number of follow max on twitter and 175_000_000 is the number of user on twitter (stats from the article)
            long max = 0;
//            long max = (long) ((long) nbUsers * ratio);

            int i = 0, nbMax = 0, avg = 0;;

            for (int val: data){
                if (val >= max) {
                    max = val;
//                    nbMax++;
//                    data.set(i, (int) max);
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
            System.out.println("max : " + Collections.max(data) + " => " + Collections.max(data)/ (double) max * 100 +"%");
            System.out.println("Q1 : " + data.get(data.size()/4) + " => " + data.get(data.size()/4) / (double) max * 100 +"%");
            System.out.println("médiane : " + data.get(data.size()/2) + " => " + data.get(data.size()/2) / (double) max * 100 +"%");
            System.out.println("Q3 : " + data.get(3*data.size()/4) + " => " + data.get(3*data.size()/4) / (double) max * 100 +"%");
            System.out.println("nbMax : " + nbMax);
            System.out.println("avg : " + avg/data.size());

        }

    }

}
