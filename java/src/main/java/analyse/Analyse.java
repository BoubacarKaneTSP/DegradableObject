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
    @Option(name = "-dlimit", usage = "distribution minimum")
    private int _dlimit = 0;
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

            if ((_projects.isEmpty() || _projects.contains(elements[0])) && elements.length >= 5){
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
                        method = method.charAt(0) == '+' ? method.substring(1) : method;
                        mapAllObj.get(elements[1]).putIfAbsent(method, new HashMap<>());
                        mapAllObj.get(elements[1]).get(method).compute(elements[0], (key, val) -> (val == null) ? 1 : val + 1);
                        sumMethodsObj.compute(elements[1], (key, val) -> (val == 0) ? 1 : val + 1 );
                        if(_tuple)
                            mapProject.get(elements[0]).get(elements[1]).get(elements[2]).add(method);
                    }
                }
            }
        }

        if (_d){

            AbstractList<String> projectsPresent = new ArrayList<>();

            for (String object : mapAllObj.keySet()){
                for (String method : mapAllObj.get(object).keySet()) {
                    for (String project : mapAllObj.get(object).get(method).keySet()) {
                        if (!projectsPresent.contains(project)){
                            projectsPresent.add(project);
                        }
                    }
                }
            }

            for (String object : mapAllObj.keySet()){
                FileWriter fileWriter = new FileWriter(object+"_graph.tex");
                PrintWriter printWriter = new PrintWriter(fileWriter);
                Double[] distribution = new Double[mapAllObj.get(object).keySet().size()];
                int nbMethods = 0;
                String begin, end, label = "", title, plot = "";
                begin = "\\begin{tikzpicture}\n" +
                        "\\begin{axis} [xbar, xmin=0, xmax=100, axis x line=bottom, axis y line=left, " +
                        "enlarge y limits=true, grid=major, xlabel={usage \\%}, ytick=data, " +
                        "yticklabels={";
                title = ", title={"+object+"}]\n";
                plot += "\\addplot coordinates { ";
                for (String method : mapAllObj.get(object).keySet()){

                    int sumMethods = 0;
                    for (String project : projectsPresent){

                        for (String m : mapAllObj.get(object).keySet()){
                            if (mapAllObj.get(object).get(m).containsKey(project)) {
                                sumMethods += mapAllObj.get(object).get(m).get(project);
                            }
                        }
                    }

                    int methodquantity = 0;

                    for (String project : projectsPresent){
                        if (mapAllObj.get(object).get(method).containsKey(project))
                            methodquantity += mapAllObj.get(object).get(method).get(project);
                    }

                    double d = round( methodquantity /(double) sumMethods * 100,2);
                    if (d>=_dlimit) {
                        distribution[nbMethods] = d;

                        label += method;
                        if (nbMethods + 1 < mapAllObj.get(object).keySet().size())
                            label += ", ";
                    }
                    nbMethods ++;
                }

//                Arrays.sort(distribution);
                for (int j = 0, i = 0; i < distribution.length; i++) {
                    if (distribution[i] != null) {
                        plot += "(" + distribution[i] + "," + j + ")";
                        j++;
                    }
                }
                label += "}";
                end = "};\n" +
                        "\\end{axis}\n" +
                        "\\end{tikzpicture}\n";

                printWriter.print(begin+label+title+plot+end);
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
                        String methodName;
                        String coordinates = "", begin, label = "", title, end;
                        int i = 0;

                        begin = "\\begin{tikzpicture}\n" +
                                "\\begin{axis}[scatter/classes={U={mark=+,red}, NU={mark=x,blue}}, legend pos=outer north east," +
                                "axis x line=bottom, axis y line=left, enlarge x limits=true, " +
                                "xlabel = {Classes}, ylabel = {Methods}, " +
                                "enlarge y limits=true, xtick = data, xticklabels = {,,}," +
                                "ytick=data, yticklabels={";

                        title = "}, title={Use of "+obj+" in "+project+"}]\n" +
                                "\\addplot[scatter,only marks, scatter src=explicit symbolic]\n" +
                                "coordinates {\n";

                        int nbtuple = 0;
                        int nbclass = 0;
                        String method1, method2;

                        switch (obj){
                            case "ConcurrentSkipListSet" :
                                method1 = "add";
                                method2 = "clear";
                                break;
                            case "AtomicLong" :
                                method1 = "get";
                                method2 = "incrementAndGet";
                                break;
                            case "ConcurrentHashMap" :
                                method1 = "get";
                                method2 = "put";
                                break;
                            case "ConcurrentLinkedQueue" :
                                method1 = "add";
                                method2 = "poll";
                                break;
                            default:
                                method1 = method2 = "";
                        }


                        for (String clazz :  mapProject.get(project).get(obj).keySet()){

                            nbclass++;
                            boolean flag = false;
                            boolean presenceMethod1 = false, presenceMethod2 = false; // TBD

//                            System.out.println(method1 + " " + method2);
                            for (String method : mapProject.get(project).get(obj).get(clazz)) {
                                flag = true;
                                methodName = method.charAt(0) == '+' ? method.substring(1): method ;

                                if (methodName.equals(method1))
                                    presenceMethod1 = true;
                                else if (methodName.equals(method2))
                                    presenceMethod2 = true;

                                if (!methodsUsed.contains(methodName))
                                    methodsUsed.add(methodName);
                                if (method.charAt(0) == '+')
                                    coordinates += "(" + i + "," + methodsUsed.indexOf(methodName) + ") [U]\n";
                                else
                                    coordinates += "(" + i + "," + methodsUsed.indexOf(methodName) + ") [NU]\n";
                            }

                            if (presenceMethod1 && presenceMethod2)
                                nbtuple++;

                            if (flag)
                                i++;
                        }

                        System.out.println(project);
                        System.out.println("    "+obj);
                        System.out.println("        nb tuples : " + nbtuple);
                        System.out.println("        nb class : " + nbclass);
                        System.out.println("        ratio : " + round(nbtuple/(double)nbclass, 2) * 100);
                        for (int j = 0; j < methodsUsed.size(); j++) {
                            label+= methodsUsed.get(j);
                            if (j+1<methodsUsed.size())
                                label+=",";
                        }

                        end = "};\n" +
//                                "\\addlegendentry{Used}\n" +
//                                "\\addlegendentry{Not used}\n" +
                                "\\end{axis}\n" +
                                "\\end{tikzpicture}";

                        printWriter.print(begin+label+title+coordinates+end);
                        printWriter.close();
                    }
                }
            }
        }

        if (_stack){

            AbstractList<String> colorList = new ArrayList<>(Arrays.asList("BurntOrange", "TealBlue", "Brown", "Dandelion", "brown", "cyan", "darkgray", "gray", "teal", "lime", "magenta", "olive", "orange", "pink", "purple", "violet", "yellow", "Apricot", "Bittersweet", "BrickRed", "Red", "Tan", "Sepia", "Goldenrod", "GreenYellow", "YellowGreen", "RubineRed", "Thistle", "Rhodamine", "DarkOrchid", "Mulberry", "RoyalBlue", "PineGreen", "RawSienna", "Mahogany", "Salmon", "YellowOrange"));
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
                System.out.println(object);
                System.out.println(mapAllObj.get(object));
                System.out.println();
                FileWriter fileWriter = new FileWriter(object+"_stack_graph.tex");
                PrintWriter printWriter = new PrintWriter(fileWriter);
                AbstractList<String> methodsUsed = new ArrayList<>();
                String methodName;
                int nbMethods = 0, color = 0;
                String begin, legend, end, plot = "";
                begin = "\\begin{tikzpicture}\n" +
                        "\\begin{axis}[ybar stacked, axis x line=bottom, axis y line=left, " +
                        "enlarge x limits=true, enlarge y limits=true, grid=minor, xlabel={Projects}, " +
                        "ylabel={Usage \\%},legend columns=2, legend pos=outer north east, xtick=data, xticklabels={" +
                        label.substring(0, label.length() - 1) +
                        "}, title={"+object+"}]\n";
                legend = "\\legend{";
                for (String method : mapAllObj.get(object).keySet()){

                    legend += method;
                    if (nbMethods+1 < mapAllObj.get(object).keySet().size())
                        legend += ", ";
                    methodName = method.charAt(0) == '+' ? method.substring(1): method ;
                    if (!methodsUsed.contains(methodName))
                        methodsUsed.add(methodName);

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
                    color++;
                    nbMethods ++;
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
