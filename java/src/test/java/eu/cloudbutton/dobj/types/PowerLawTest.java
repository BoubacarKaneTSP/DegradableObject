package eu.cloudbutton.dobj.types;


import nl.peterbloem.powerlaws.DiscreteApproximate;
import org.apache.commons.math3.distribution.ParetoDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.RandomGeneratorFactory;
import org.testng.annotations.Test;

import java.util.*;

public class PowerLawTest {

    @Test
    void add() {

        double SCALE = 20000, SHAPE = 1.35;
        int numValues = 1000000;
        List<Double> doubleValues = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
        double desiredMaxValue;

        if ((numValues*0.43)/100 <= 0)
            desiredMaxValue = numValues/2;
        else
            desiredMaxValue = (numValues*0.43)/100;

        RandomGenerator rand = RandomGeneratorFactory.createRandomGenerator(new Random(94));
        ParetoDistribution distribution = new ParetoDistribution(rand,SCALE,SHAPE);

        double maxGeneratedValue = 0;
        for (int i = 0; i < numValues; i++) {
            double randomValue = distribution.sample();
            doubleValues.add(randomValue);
            if (randomValue > maxGeneratedValue) {
                maxGeneratedValue = randomValue;
            }
        }

        double scaleFactor = desiredMaxValue / maxGeneratedValue;

        for (int i = 0; i < numValues; i++) {
            double scaledValue = doubleValues.get(i) * scaleFactor;
            values.add((int) Math.round(scaledValue));
        }

        List<Double> listAlpha = new ArrayList<>();

        /*for (double i = 1.315 ; i <= 1.315; i+=0.025) {
            listAlpha.add(i);
        }*/


        int nbUsers= 100000;

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
        for (int val: values){
            if (val >= maxFollower) {
                nbMax++;
            }
            if (val>= 0.5*maxFollower)
                j++;
            if (val < 0) {
                values.set(i, 1);
            }

            avg += values.get(i);
            i++;
        }

        Collections.sort(values);

//            System.out.println(values);
        System.out.println("nb half max : " + j);
        System.out.println();
        System.out.println("max : " + Collections.max(values) + " => " + Collections.max(values)/ (double) maxFollower * 100 +"%");
        System.out.println("Q1 : " + values.get(values.size()/4) + " => " + values.get(values.size()/4) / (double) maxFollower * 100 +"%");
        System.out.println("médiane : " + values.get(values.size()/2) + " => " + values.get(values.size()/2) / (double) maxFollower * 100 +"%");
        System.out.println("Q3 : " + values.get(3*values.size()/4) + " => " + values.get(3*values.size()/4) / (double) maxFollower * 100 +"%");
        System.out.println("nbMax : " + nbMax);
        System.out.println("avg : " + avg/values.size());



    }

}
