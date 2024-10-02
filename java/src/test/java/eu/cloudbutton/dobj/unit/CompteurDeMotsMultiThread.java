package eu.cloudbutton.dobj.unit;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
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

        ExecutorService executor = Executors.newFixedThreadPool(nbThreads);
        ArrayList<Runnable> taches = new ArrayList<>();
        Map<String, AtomicInteger> compteurGlobale = new ConcurrentHashMap<>();
        String regex = "^[a-zA-Z-]+$";

        for (int i = 0; i < nbThreads; i++) {

            String[] mots = textes.get(i).toString().replaceAll("[^a-zA-Z\\s-]", "").trim().split("\\s+");

            taches.add(() -> {
                int taille = mots.length;
                for (int j = 0; j < taille; j++) {
                      String mot = mots[j].replaceAll("^[^a-zA-Z-]+|[^a-zA-Z-]+$", "");
                      if (mot.matches(regex)) {
                            compteurGlobale.merge(mot, new AtomicInteger(1), (ancien, _) -> {
                                ancien.incrementAndGet();
                                return ancien;
                            });
                      }
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
        System.out.println("Temps de calcul : " + (double) totalTime / 1_000_000_000 + " seconds");

        int totalMots = compteurGlobale.values().stream().mapToInt(AtomicInteger::get).sum();
        System.out.println("Nombre total de mots : " + totalMots);
    }
}
