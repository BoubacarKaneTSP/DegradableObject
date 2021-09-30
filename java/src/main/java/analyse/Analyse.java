package analyse;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.ExplicitBooleanOptionHandler;

import java.io.*;
import java.util.*;

import static java.lang.Math.round;
import static org.kohsuke.args4j.OptionHandlerFilter.ALL;

public class Analyse {

    @Option(name = "-percentage", handler = ExplicitBooleanOptionHandler.class, usage = "Percentage of methods")
    private boolean _percentage = false;
    @Option(name = "-private", handler = ExplicitBooleanOptionHandler.class, usage = "Consider private classes")
    private boolean _private = false;
    @Option(name = "-file", required = true, usage = "File's name")
    private String _file;
    @Option(name = "-limit", usage = "number of methods analyzed")
    private int _limit = 10;

    public static void main(String[] args) throws IOException {
        new Analyse().doMain(args);
    }

    public void doMain(String[] args) throws IOException {
        CmdLineParser parser = new CmdLineParser(this);

        try{
            parser.parseArgument(args);

        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            System.err.println("java SampleMain [options...] arguments...");

            parser.printUsage(System.err);
            System.err.println();

            System.err.println("  Example: java Benchmark" + parser.printExample(ALL));

            return;
        }

        BufferedReader bufferedReader = null;
        String s;
        String[] elements;
        AbstractMap<String, AbstractMap<String, Integer>> mapObj = new HashMap<>();
        AbstractMap<String, Integer> sumMethodsObj = new HashMap<>();

        try
        {
            bufferedReader = new BufferedReader(new FileReader(_file));
        }
        catch(FileNotFoundException exc)
        {
            System.out.println("File opening error");
        }
        while ((s = bufferedReader.readLine()) != null) {
            elements = s.split(" ");
            mapObj.putIfAbsent(elements[1], new HashMap<>());
            sumMethodsObj.putIfAbsent(elements[1], 0);

            if (elements[3].equals("N") || _private) {
                for (String method : Arrays.copyOfRange(elements, 4, elements.length)) {
                    mapObj.get(elements[1]).compute(method, (key, val) -> (val == null) ? 1 : val + 1);
                    sumMethodsObj.compute(elements[1], (key, val) -> (val == 0) ? 1 : val + 1 );
                }
            }
        }

        for(String obj : mapObj.keySet()) {
            for (String ignored : mapObj.get(obj).keySet())
                mapObj.put(obj, sortByValue((HashMap<String, Integer>) mapObj.get(obj)));
        }

        String op;

        for (String obj : mapObj.keySet()){
            System.out.println("-"+obj);
            for (String method: mapObj.get(obj).keySet()) {
                System.out.print("    ");
                if(_percentage)
                    op = String.valueOf(this.round(mapObj.get(obj).get(method) / (double) sumMethodsObj.get(obj) * 100, 2));
                else
                    op = String.valueOf(mapObj.get(obj).get(method));

                System.out.println("-" + method +": "+ op);
            }
        }

        for (String obj: mapObj.keySet()){
            String[] arrayMethod = mapObj.get(obj).keySet().toArray(new String[mapObj.get(obj).size()]);
            Integer[] tmp = mapObj.get(obj).values().toArray(new Integer[mapObj.get(obj).size()]);
            Double[] arrayPercentage = new Double[mapObj.get(obj).size()];

            for (int i = 0; i < tmp.length; i++) {
                arrayPercentage[i] = this.round(tmp[i] / (double) sumMethodsObj.get(obj) * 100, 2);
            }

            FileWriter fileWriter = new FileWriter(obj+"_graph.tex");
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.print("\\begin{tikzpicture}\n" +
                    "\\begin{axis} [xbar, xmin=0, xmax=100, axis x line=bottom, axis y line=left, enlarge y limits=true, grid=major, xlabel={usage \\%}, ytick=data, yticklabels={");

            for (int i = 0; i< arrayPercentage.length && i < _limit; i++) {
                printWriter.print(arrayMethod[i]);
                if (i+1 < arrayPercentage.length && i+1 < _limit)
                    printWriter.print(",");
            }

            printWriter.printf("}, title={%s}]\n", obj);

            printWriter.print("\\addplot ");
            printWriter.print("coordinates {");
            String coordinates = "";
            for (int i = 0; i < arrayPercentage.length && i < _limit; i++) {
                coordinates = "("+arrayPercentage[i].toString()+","+i+")" + coordinates;
            }
            printWriter.print(coordinates);
            printWriter.println("};");
            printWriter.println("\\end{axis}\n" +
                    "\\end{tikzpicture}");

            printWriter.close();
        }

        bufferedReader.close();
    }

    private double round(double r, int places){
        double scale = Math.pow(10, places);
        return Math.round(r * scale) / scale;
    }

    // function to sort hashmap by values
    public static HashMap<String, Integer> sortByValue(HashMap<String, Integer> hm)
    {
        // Create a list from elements of HashMap
        List<Map.Entry<String, Integer> > list = new LinkedList<>(hm.entrySet());

        // Sort the list
        Collections.sort(list, Map.Entry.comparingByValue(Comparator.reverseOrder()));

        // put data from sorted list to hashmap
        HashMap<String, Integer> temp = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }
}
