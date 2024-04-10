package eu.cloudbutton.dobj.utils;

import eu.cloudbutton.dobj.key.Key;

import java.util.*;

public class Map {
    public static java.util.Map<Key, Integer> sortMapByValue(java.util.Map<Key, Integer> inputMap) {
        // Convert the inputMap to a List of Map.Entry objects
        List<java.util.Map.Entry<Key, Integer>> entryList = new ArrayList<java.util.Map.Entry<Key, Integer>>(inputMap.entrySet());

        // Sort the entryList using a custom comparator based on values
        Collections.sort(entryList, Comparator.comparing(java.util.Map.Entry::getValue));
        Collections.reverse(entryList);
        // Create a new LinkedHashMap to store the sorted entries
        java.util.Map<Key, Integer> sortedMap = new LinkedHashMap<Key, Integer>();
        for (java.util.Map.Entry<Key, Integer> entry : entryList) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }
}