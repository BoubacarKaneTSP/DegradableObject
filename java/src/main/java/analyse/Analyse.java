package analyse;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.ExplicitBooleanOptionHandler;
import org.kohsuke.args4j.spi.StringArrayOptionHandler;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.lang.Math.round;
import static org.kohsuke.args4j.OptionHandlerFilter.ALL;

public class Analyse {

    @Option(name = "-percentage", handler = ExplicitBooleanOptionHandler.class, usage = "Percentage of methods")
    private boolean _percentage = false;
    @Option(name = "-public", handler = ExplicitBooleanOptionHandler.class, usage = "Consider private classes")
    private boolean _public = false;
    @Option(name = "-file", required = true, usage = "File's name")
    private String _file;
    @Option(name = "-limit", usage = "number of methods analyzed")
    private int _limit = 10;
    @Option(name = "-tuple", handler = ExplicitBooleanOptionHandler.class, usage = "Generate graph to see tuples")
    private boolean _tuple = false;
    @Option(name = "-stack", handler = ExplicitBooleanOptionHandler.class, usage = "Generate stack histogram to see distribution")
    private boolean _stack = false;
    @Option(name = "-d", handler = ExplicitBooleanOptionHandler.class, usage = "Generate graph to see distribution")
    private boolean _d = false;
    @Option(name = "-projects", handler = StringArrayOptionHandler.class, usage = "project (max 10)")
    private String[] _p = new String[10];

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
        AbstractSet<String> _projects = set_projects(_p);
        AbstractMap<String, AbstractMap<String, AbstractMap<String, Integer>>> mapAllObj = new HashMap<>();
        AbstractMap<String, Integer> sumMethodsObj = new HashMap<>();
        AbstractMap<String, AbstractMap<String, AbstractMap<String, AbstractSet<String>>>> mapProject = new HashMap<>();

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

            if (_projects.isEmpty() || _projects.contains(elements[0])){
                mapAllObj.putIfAbsent(elements[1], new HashMap<>());
                sumMethodsObj.putIfAbsent(elements[1], 0);

                if (_tuple) {
                    mapProject.putIfAbsent(elements[0], new HashMap<>());
                }

                if (elements[3].equals("Y") || _public) {
                    if (_tuple) {
                        mapProject.get(elements[0]).putIfAbsent(elements[1], new HashMap<>());
                        mapProject.get(elements[0]).get(elements[1]).put(elements[2], new HashSet<>());
                    }
                    for (String method : Arrays.copyOfRange(elements, 4, elements.length)) {
                        mapAllObj.get(elements[1]).putIfAbsent(method, new HashMap<>());
                        mapAllObj.get(elements[1]).get(method).compute(elements[0], (key, val) -> (val == null) ? 1 : val + 1);
                        sumMethodsObj.compute(elements[1], (key, val) -> (val == 0) ? 1 : val + 1 );
                        if(_tuple)
                            mapProject.get(elements[0]).get(elements[1]).get(elements[2]).add(method);
                    }
                }
            }
        }

/*        for(String obj : mapAllObj.keySet()) {
            for (String ignored : mapAllObj.get(obj).keySet())
                mapAllObj.put(obj, sortByValue((HashMap<String, Integer>) mapAllObj.get(obj)));
        }

        String op;
        for (String obj : mapAllObj.keySet()){
            System.out.println("-"+obj);
            for (String method: mapAllObj.get(obj).keySet()) {
                System.out.print("    ");
                if(_percentage)
                    op = String.valueOf(this.round(mapAllObj.get(obj).get(method) / (double) sumMethodsObj.get(obj) * 100, 2));
                else
                    op = String.valueOf(mapAllObj.get(obj).get(method));

                System.out.println("-" + method +": "+ op);
            }
        }*/

        if (_d){
            for (String obj: mapAllObj.keySet()){
                String[] arrayMethod = mapAllObj.get(obj).keySet().toArray(new String[mapAllObj.get(obj).size()]);
                Integer[] tmp = mapAllObj.get(obj).values().toArray(new Integer[mapAllObj.get(obj).size()]);
                Double[] arrayPercentage = new Double[mapAllObj.get(obj).size()];

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
        }

        if (_tuple){
            for (String project : mapProject.keySet()){
                if (_projects.isEmpty() || _projects.contains(project)){
                    for (String obj : mapProject.get(project).keySet()){

                        FileWriter fileWriter = new FileWriter(obj+"_"+project+"_graph.tex");
                        PrintWriter printWriter = new PrintWriter(fileWriter);
                        AbstractList<String> methodsUsed = new ArrayList<>();
                        String coordinates = "", begin, label = "", title, end;
                        int i = 0;

                        begin = "\\begin{tikzpicture}\n" +
                                "\\begin{axis}[scatter/classes={U={mark=+,red}, NU={mark=x,blue}}, legend pos=outer north east," +
                                "axis x line=bottom, axis y line=left, enlarge x limits=true, " +
                                "enlarge y limits=true, ytick=data, yticklabels={";

                        title = "}, title={Use of "+obj+" in "+project+"}]\n" +
                                "\\addplot[scatter,only marks, scatter src=explicit symbolic]\n" +
                                "coordinates {\n";

                        for (String clazz :  mapProject.get(project).get(obj).keySet()){

                            for (String method : mapProject.get(project).get(obj).get(clazz)) {
                                if (!methodsUsed.contains(method))
                                    methodsUsed.add(method);
                                if (method.charAt(0) == '+')
                                    coordinates += "(" + i + "," + methodsUsed.indexOf(method) + ") [U]\n";
                                else
                                    coordinates += "(" + i + "," + methodsUsed.indexOf(method) + ") [NU]\n";
                            }

                            i++;
                        }

                        for (int j = 0; j < methodsUsed.size(); j++) {
                            label+=methodsUsed.get(j);
                            if (j+1<methodsUsed.size())
                                label+=",";
                        }

                        end = "};\n" +
                                "\\addlegendentry{Used}\n" +
                                "\\addlegendentry{Not used}\n" +
                                "\\end{axis}\n" +
                                "\\end{tikzpicture}";

                        printWriter.print(begin+label+title+coordinates+end);
                        printWriter.close();
                    }
                }
            }
        }

        if (_stack){

            AbstractList<String> colorList = new ArrayList<>(Arrays.asList("black", "blue", "brown", "cyan", "darkgray", "gray", "green", "lightgray", "lime", "magenta", "olive", "orange", "pink", "purple", "red", "teal", "violet", "yellow"));
            AbstractList<String> projectsPresent = new ArrayList<>();
            String label = "";
            for (String object : mapAllObj.keySet()){
                for (String method : mapAllObj.get(object).keySet()) {
                    for (String project : mapAllObj.get(object).get(method).keySet()) {
                        if (!projectsPresent.contains(project)){
                            projectsPresent.add(project);
                            label += project+",";
                        }
                    }
                }
            }

            for (String object : mapAllObj.keySet()){
                FileWriter fileWriter = new FileWriter(object+"_stack_graph.tex");
                PrintWriter printWriter = new PrintWriter(fileWriter);
                int nbMethods = 0, color = 0;
                String begin, legend, end, plot = "";
                begin = "\\begin{tikzpicture}\n" +
                        "\\begin{axis}[ybar stacked, axis x line=bottom, axis y line=left, " +
                        "enlarge x limits=true, enlarge y limits=true, grid=minor, xlabel={Projects}, " +
                        "ylabel={usage \\%},legend columns=2, legend pos=outer north east, xtick=data, xticklabels={" +
                        label.substring(0, label.length() - 1) +
                        "}, title={"+object+"}]\n";
                legend = "\\legend{";
                for (String method : mapAllObj.get(object).keySet()){
                    legend += method;
                    if (nbMethods+1 < mapAllObj.get(object).keySet().size())
                        legend += ",";
                    plot += "\\addplot[fill, color=" + colorList.get(color%colorList.size()) +"] coordinates\n    { ";
                    for (String project : projectsPresent){
                        int sumMethods = 0;
                        for (String m : mapAllObj.get(object).keySet()){
                           if (mapAllObj.get(object).get(m).containsKey(project))
                                sumMethods += mapAllObj.get(object).get(m).get(project);
                        }

                        plot += mapAllObj.get(object).get(method).get(project) == null ?
                                "("+projectsPresent.indexOf(project)+", 0.00) " :
                                "("+projectsPresent.indexOf(project)+","+ round( mapAllObj.get(object).get(method).get(project) /(double) sumMethods * 100,2)+") " ;
                    }
                    plot += "};\n";
                    nbMethods ++;
                    color++;
                }

                end = "};\n" +
                        "\\end{axis}\n" +
                        "\\end{tikzpicture}\n";

                printWriter.print(begin+plot+legend+end);
                printWriter.close();
            }
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

    public AbstractSet<String> set_projects(String[] p){
        AbstractSet<String> _projects = new HashSet<>();
        for (String project : p)
            if (project != null)
                _projects.add(project);

        return _projects;
    }
}
