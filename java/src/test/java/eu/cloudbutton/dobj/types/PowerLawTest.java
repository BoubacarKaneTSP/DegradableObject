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

        int nbUsers= 100000;

        for (double alpha : listAlpha){
            List<Integer> data = new DiscreteApproximate(1, alpha).generate(nbUsers);

            int maxFollower, maxFollowing;

            maxFollower = (int) ((0.84 * nbUsers)/100);
            maxFollowing = (int) ((0.043 * nbUsers)/100);

            System.out.println("maxFollowers = " + maxFollower);
            System.out.println("maxFollowing = " + maxFollowing);

            double ratio = 100000 / 175000000.0; //10⁵ is ~ the number of follow max on twitter and 175_000_000 is the number of user on twitter (stats from the article)
//            long max = 0;
            System.out.println(ratio);
            System.out.println(nbUsers);
            System.out.println(maxFollower);
            int i = 0, nbMax = 0, avg = 0;;
            int j = 0;
            for (int val: data){
                if (val >= maxFollower) {
                    nbMax++;
                    data.set(i, maxFollower);
                }
                if (val>= 0.5*maxFollower)
                    j++;
                if (val < 0) {
                    data.set(i, 1);
                }

                avg += data.get(i);
                i++;
            }

            Collections.sort(data);

//            System.out.println(data);
            System.out.println("nb half max : " + j);
            System.out.println();
            System.out.println("======= " + alpha + " =======");
            System.out.println("max : " + Collections.max(data) + " => " + Collections.max(data)/ (double) maxFollower * 100 +"%");
            System.out.println("Q1 : " + data.get(data.size()/4) + " => " + data.get(data.size()/4) / (double) maxFollower * 100 +"%");
            System.out.println("médiane : " + data.get(data.size()/2) + " => " + data.get(data.size()/2) / (double) maxFollower * 100 +"%");
            System.out.println("Q3 : " + data.get(3*data.size()/4) + " => " + data.get(3*data.size()/4) / (double) maxFollower * 100 +"%");
            System.out.println("nbMax : " + nbMax);
            System.out.println("avg : " + avg/data.size());

        }

    }

}
