package analyse;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.ExplicitBooleanOptionHandler;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static java.lang.Math.round;
import static org.kohsuke.args4j.OptionHandlerFilter.ALL;

public class Analyse {

    @Option(name = "-percentage", handler = ExplicitBooleanOptionHandler.class, usage = "Percentage of methods")
    private boolean _percentage = false;
    @Option(name = "-private", handler = ExplicitBooleanOptionHandler.class, usage = "Consider private classes")
    private boolean _private = false;
    @Option(name = "-file", required = true, usage = "File's name")
    private String _file;

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
        bufferedReader.close();

    }

    private double round(double r, int places){
        double scale = Math.pow(10, places);
        return Math.round(r * scale) / scale;
    }
}
