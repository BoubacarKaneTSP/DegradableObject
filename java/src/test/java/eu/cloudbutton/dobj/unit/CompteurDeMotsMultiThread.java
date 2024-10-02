package eu.cloudbutton.dobj.unit;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CompteurDeMotsMultiThread {
    public static void main(String[] args) throws IOException {

        String fileName = "experiences/LoremIpsum.txt";
        String texte = "";
        try{
            texte = Files.readString(Paths.get(fileName));
        }catch (IOException e) {
            e.printStackTrace();
        }
        File fichier = new File(fileName);

        FileReader fileReader = new FileReader(fichier);

        BufferedReader bufferedReader = new BufferedReader(fileReader);
        bufferedReader.readLine();

        texte = texte.replaceAll("[^a-zA-Z\\s-]", "");
        String[] mots = texte.trim().split("\\s+");
        System.out.println(mots.length);
        int nbThreads = Runtime.getRuntime().availableProcessors();
        int tailleDeSegment = (int) Math.ceil((double) mots.length / nbThreads);

        ExecutorService executor = Executors.newFixedThreadPool(nbThreads);
        ArrayList<Runnable> taches = new ArrayList<>();
        Map<String, AtomicInteger> compteurGlobale = new ConcurrentHashMap<>();
        String regex = "^[a-zA-Z-]+$";

        for (int i = 0; i < nbThreads; i++) {
            final int debut = i * tailleDeSegment;
            final int fin = Math.min(debut + tailleDeSegment, mots.length);
            taches.add(() -> {
                for (int j = debut; j < fin; j++) {
                      String mot = mots[j].replaceAll("^[^a-zA-Z-]+|[^a-zA-Z-]+$", "");
                      if (mot.matches(regex)) {
                            compteurGlobale.merge(mot, new AtomicInteger(1), (ancien, _) -> {ancien.incrementAndGet();
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
