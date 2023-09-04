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

        double SCALE = 100000, SHAPE = 10;
        int numValues = 1000000;
        List<Double> doubleValues = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
        double desiredMaxValue;

        if ((numValues*8.4)/100 <= 0)
            desiredMaxValue = numValues/2;
        else
            desiredMaxValue = (numValues*8.4)/100;

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
            double scaledValue = doubleValues.get(i) ;//* scaleFactor;
            values.add((int) Math.round(scaledValue));
        }

        System.out.println(Collections.max(values));

        int maxFollower, maxFollowing;

        maxFollower = (int) (0.084 * numValues);
        maxFollowing = (int) (0.0043 * numValues);

        System.out.println("maxFollowers = " + maxFollower);
        System.out.println("maxFollowing = " + maxFollowing);

        int i = 0, nbMax = 0;
        long avg = 0;
        int j = 0;

        Map<Integer, Integer> countValues = new HashMap<>();

        for (int val: values){
            /*if (val >= maxFollower) {
                nbMax++;
            }
            if (val>= 0.5*maxFollower)
                j++;
            if (val < 0) {
                values.set(i, 1);
            }*/

            avg += values.get(i);
            i++;

            if (!countValues.containsKey(val)){
                countValues.put(val, 1);
            }else {
                countValues.put(val, countValues.get(val) + 1);
            }
        }

        countValues = sortMapByValue(countValues);
//        System.out.println(countValues.values());

        for (int v : countValues.values()){
            if (v>1)
                System.out.println(v);
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


 /*       Map<String,Integer> mapTest = new HashMap<>();

        mapTest.put("D", 1);
        mapTest.put("C", 2);
        mapTest.put("B", 3);
        mapTest.put("A", 4);

        System.out.println(mapTest);*/
    }

    public static Map<Integer, Integer> sortMapByValue(Map<Integer, Integer> inputMap) {
        // Convert the inputMap to a List of Map.Entry objects
        List<Map.Entry<Integer, Integer>> entryList = new ArrayList<>(inputMap.entrySet());

        // Sort the entryList using a custom comparator based on values
        Collections.sort(entryList, Comparator.comparing(Map.Entry::getValue));

        // Create a new LinkedHashMap to store the sorted entries
        Map<Integer, Integer> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<Integer, Integer> entry : entryList) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }
}
