package eu.cloudbutton.dobj.utils;

import eu.cloudbutton.dobj.benchmark.Retwis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class HeapDump {

    public static void performHeapDump(String tag, String when, int nbUser) {
        System.out.println("Performing heapDump");
        String jcmdCommand = "jcmd";
        String processId = getProcessId();

        try {
            Process process = Runtime.getRuntime().exec(jcmdCommand);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            while ((line = reader.readLine()) != null) {
                // Recherche de la ligne contenant le processus Java souhaité
                if (line.contains(processId)) {
                    String[] tokens = line.trim().split("\\s+");
                    String pid = tokens[0];
                    String heapDumpCommand = "jcmd " + pid + " GC.heap_dump " + "heapdump_" + when + "_benchmark_" + tag + "_" + nbUser + ".hprof";

                    // Exécution de la commande jcmd pour effectuer le heap dump
                    Process heapDumpProcess = Runtime.getRuntime().exec(heapDumpCommand);

                    // Attente de la fin de l'exécution de la commande
                    int exitCode = heapDumpProcess.waitFor();

                    if (exitCode == 0) {
                        System.out.println("Heap dump effectué avec succès !");
                    } else {
                        System.out.println("Erreur lors de l'exécution de la commande jcmd.");
                    }

                    break;
                }
            }

            reader.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static String getProcessId() {
        String processName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
        return processName.split("@")[0];
    }
}