package eu.cloudbutton.dobj.unit;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


public class CompteurDeMotsMultiThread {

    static boolean detailed = true;

    public static void main(String[] args) throws IOException {
//        String fileName = "experiences/LoremIpsum.txt";
        String fileName = "/home/bkane/IdeaProjects/DegradableObject/experiences/LoremIpsum.txt";
        int nbThreads = Runtime.getRuntime().availableProcessors();
        ArrayList<StringBuilder> textes = new ArrayList<>();
        for (int i = 0; i < nbThreads; i++)
            textes.add(new StringBuilder());

        String regex = "^[a-zA-Z-]+$";

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))){
            String ligne;
            long i = 0;
            while ((ligne = reader.readLine()) != null) {
                if (!ligne.isEmpty() || ligne.matches(regex)) {
                    textes.get((int) (i % nbThreads)).append(ligne.replaceAll("^[^a-zA-Z-]+|[^a-zA-Z-]+$", ""));
                    i++;
                }
            }
        }

        ArrayList<String[]> mots = new ArrayList<>();

        for (int i = 0; i < nbThreads; i++)
            mots.add(textes.get(i).toString().replaceAll("[^a-zA-Z\\s-]", "").trim().split("\\s+"));


        int nbTest = 1;
        long val_synchronyzed = 0, val_obj_conc = 0;

        for (int i = 0; i < nbTest; i++) {
//            System.out.print("sync : ");
            val_synchronyzed += parallel_synchronyzed(mots, nbThreads);
//            System.out.print("conc : ");
            val_obj_conc += parallel_conc_obj(mots, nbThreads);
        }

        System.out.println("Temps de calcul synchronyzed: " + (double) (val_synchronyzed/nbTest) / 1_000_000_000 + " seconds");
        System.out.println("Temps de calcul objet concurrent: " + (double) (val_obj_conc/nbTest) / 1_000_000_000 + " seconds");

//        long totalMots = compteurGlobale.values().stream().mapToInt(AtomicInteger::get).sum();
//        System.out.println("Nombre total de mots : " + totalMots);
    }

    public static long parallel_conc_obj(ArrayList<String[]> mots, int nbThreads){
        ExecutorService executor = Executors.newFixedThreadPool(nbThreads);
        ArrayList<Runnable> taches = new ArrayList<>();
        Map<String, AtomicInteger> compteurGlobale = new ConcurrentHashMap<>();
        AtomicInteger counter = new AtomicInteger(0);

        if (detailed)
            System.out.println("compute concurrent obj : \n");

        for (int i = 0; i < nbThreads; i++) {
            if (detailed)
                System.out.println("nb de mot à compter par le thread "+i+" :"+mots.get(i).length);
            int finalI = i;
            taches.add(() -> {
                HashMap<String,Integer> compteurLocale = new HashMap<>();
                for (String mot : mots.get(finalI))
                    compteurLocale.merge(mot, 1,Integer::sum);

                compteurLocale.forEach((m, val) -> compteurGlobale.merge(m, new AtomicInteger(1), (ancien, _) -> {
                    ancien.addAndGet(val);
                    return ancien;
                }));

                if (detailed)
                    System.out.println("nb de mot different pour le thread "+finalI+" : " + compteurLocale.size());
            });
        }

        if (detailed)
            System.out.println("\n");

        long totalTime, endTime, startTime = System.nanoTime();

        for (Runnable task : taches)
            executor.execute(task);

        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        endTime = System.nanoTime();
        totalTime = endTime - startTime;

        if (detailed)
            System.out.println("\n");

        return totalTime;
    }

    public static long parallel_synchronyzed(ArrayList<String[]> mots, int nbThreads){
        ExecutorService executor = Executors.newFixedThreadPool(nbThreads);
        ArrayList<Runnable> taches = new ArrayList<>();
        Map<String, Integer> compteurGlobale = new HashMap<>();
        AtomicInteger counter = new AtomicInteger(0);

        if (detailed)
            System.out.println("compute synchronyzed : \n");

        for (int i = 0; i < nbThreads; i++) {
            if (detailed)
                System.out.println("nb de mot à compter par le thread "+i+" :"+mots.get(i).length);
            int finalI = i;
            taches.add(() -> {
                HashMap<String,Integer> compteurLocale = new HashMap<>();

                for (String mot : mots.get(finalI))
                    compteurLocale.merge(mot, 1, Integer::sum);

                synchronized (compteurGlobale) {
                    compteurLocale.forEach((m, c) -> compteurGlobale.merge(m, c, Integer::sum));
                }

                if (detailed)
                    System.out.println("nb de mot different pour le thread "+finalI+" : " + compteurLocale.size());
            });
        }

        if (detailed)
            System.out.println("\n");

        long totalTime, endTime, startTime = System.nanoTime();

        for (Runnable task : taches)
            executor.execute(task);

        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (detailed)
            System.out.println("\n");

        endTime = System.nanoTime();
        totalTime = endTime - startTime;

        return totalTime;
    }

}
