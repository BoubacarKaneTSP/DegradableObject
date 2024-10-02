package eu.cloudbutton.dobj.unit;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CompteurDeMotsMultiThread {
    public static void main(String[] args) throws IOException {

//        String fileName = "experiences/LoremIpsum.txt";
        String fileName = "/home/bkane/IdeaProjects/DegradableObject/experiences/LoremIpsum.txt";
        int nbThreads = Runtime.getRuntime().availableProcessors();
        ArrayList<StringBuilder> textes = new ArrayList<>();
        for (int i = 0; i < nbThreads; i++)
            textes.add(new StringBuilder());

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))){
            String ligne;
            long i = 0;
            while ((ligne = reader.readLine()) != null) {
                textes.get((int) (i%nbThreads)).append(ligne);
                i++;
            }
        }

//        long totalTime = parallel_conc_obj(textes, nbThreads);
        int nbTest = 10;
        long val_synchronyzed = 0, val_obj_conc = 0;

        for (int i = 0; i < nbTest; i++) {
            val_synchronyzed += parallel_synchronyzed(textes, nbThreads);
            val_obj_conc += parallel_conc_obj(textes, nbThreads);
        }

        System.out.println("Temps de calcul synchronyzed: " + (double) (val_synchronyzed/nbTest) / 1_000_000_000 + " seconds");
        System.out.println("Temps de calcul objet concurrent: " + (double) (val_obj_conc/nbTest) / 1_000_000_000 + " seconds");

//        long totalMots = compteurGlobale.values().stream().mapToInt(AtomicInteger::get).sum();
//        System.out.println("Nombre total de mots : " + totalMots);
    }

    public static long parallel_conc_obj(ArrayList<StringBuilder> textes, int nbThreads){
        ExecutorService executor = Executors.newFixedThreadPool(nbThreads);
        ArrayList<Runnable> taches = new ArrayList<>();
        Map<String, Integer> compteurGlobale = new ConcurrentHashMap<>();
        String regex = "^[a-zA-Z-]+$";

        for (int i = 0; i < nbThreads; i++) {

            String[] mots = textes.get(i).toString().replaceAll("[^a-zA-Z\\s-]", "").trim().split("\\s+");

            taches.add(() -> {
                int taille = mots.length;
                for (int j = 0; j < taille; j++) {
                    String mot = mots[j].replaceAll("^[^a-zA-Z-]+|[^a-zA-Z-]+$", "");
                    compteurGlobale.merge(mot, 1, Integer::sum);
//                    if (mot.matches(regex)) {
//                        compteurGlobale.merge(mot, new AtomicInteger(1), (ancien, _) -> {
//                            ancien.incrementAndGet();
//                            return ancien;
//                        });
//                    }
                }
            });
        }

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

        return totalTime;
    }

    public static long parallel_synchronyzed(ArrayList<StringBuilder> textes, int nbThreads){
        ExecutorService executor = Executors.newFixedThreadPool(nbThreads);
        ArrayList<Runnable> taches = new ArrayList<>();
        Map<String, Integer> compteurGlobale = new HashMap<>();
        String regex = "^[a-zA-Z-]+$";

        for (int i = 0; i < nbThreads; i++) {

            String[] mots = textes.get(i).toString().replaceAll("[^a-zA-Z\\s-]", "").trim().split("\\s+");

            taches.add(() -> {
                HashMap<String,Integer> compteurLocale = new HashMap<>();

                int taille = mots.length;
                for (int j = 0; j < taille; j++) {
                    String mot = mots[j].replaceAll("^[^a-zA-Z-]+|[^a-zA-Z-]+$", "");
                    if (mot.matches(regex)) {
                        compteurLocale.merge(mot, 1, Integer::sum);
                    }
                }

                synchronized (compteurGlobale) {
                    compteurLocale.forEach((m, c) -> compteurGlobale.merge(m, c, Integer::sum));
                }
            });
        }

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

        return totalTime;
    }

}
