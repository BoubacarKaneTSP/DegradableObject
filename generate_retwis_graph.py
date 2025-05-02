import os
import argparse
import statistics

JAVA = "juc"
DEG = "dego"
DAP = "dap"
DEFAULT_NBUSER = "1000000"
# DEFAULT_NBUSER = "100000"
DEFAULT_ALPHA = "1"
DEFAULT_NBTHREAD = "40"
# DEFAULT_NBTHREAD = "80"

def setUp_parser():
    parser = argparse.ArgumentParser(description='Generate a latex graph from the values in the files')
    parser.add_argument('-p', '--perfseq', dest='perf_seq', type=str, help='sequential perf')
    parser.add_argument('-t', '--type_figure', dest='type_figure', type=int, help='Figure type generated (1 for perf relative to baseline, 2 for perf with different alpha)')
    parser.add_argument('-f', '--file', dest='perf_file', type=str, help='perf file')
    parser.add_argument('-d', '--dap', dest='perf_dap', type=str, help='perf dap file')
    parser.add_argument('-o', '--output', dest='output_file', type=str, help='output perf file')
    parser.add_argument('-a', '--area', dest='obj_perf_files', action='append', type=str, help="different file to build area plots")

    args = parser.parse_args()

    return args

def get_list_nbThread(dico: dict) -> list:
    key_list1 = list(dico[JAVA].keys())
    key_list2 = list(dico[JAVA][key_list1[0]].keys())
    return list(dico[JAVA][key_list1[0]][key_list2[0]].keys())

def get_list_nbUser(dico: dict) -> list:
    key_list = list(dico[JAVA].keys())
    return list(dico[JAVA][key_list[0]].keys())

def get_list_alpha(dico: dict) -> list:
    return list(dico[JAVA].keys())

def get_list_typeObj(dico: dict) -> list:
    return list(dico.keys())


def get_perf_values(list_values: list):

    if type(list_values[0]) != float:
        int_list_values = [float(val.strip()) for val in list_values]
    else:
        int_list_values = list_values

    if len(int_list_values) > 1:
        standard_deviation = statistics.stdev(int_list_values)
        margin_of_error = 1.96 * standard_deviation / len(int_list_values) ** 0.5
        mean = statistics.mean(int_list_values)

        return (round(mean,4), margin_of_error)
    else:
        return (int_list_values[0], 0)


def compute_speedup(perf_java, perf, margin):
    return (round(float(perf)/float(perf_java),4), round(float(margin)/float(perf_java),4))

def get_str_list_xtick(list_val: list) -> str:

    str_list_val = ""

    for val in list_val:
        str_list_val += str(val) + ','

    if str_list_val == "":
        print("Error while getting str list val")
        exit(1)

    return str_list_val

def set_bar_shift(type_obj, pos):

    bar_shift = ""

    if type_obj == JAVA:
        if pos == "bot":
            bar_shift = "-.6cm"
        elif pos == "mid":
            bar_shift = ".175cm"
        elif pos == "top":
            bar_shift = ".975cm"
    elif type_obj == DEG:
        if pos == "bot":
            bar_shift = "-.975cm"
        elif pos == "mid":
            bar_shift = "-.175cm"
        elif pos == "top":
            bar_shift = ".6cm"

    if bar_shift == "":
        print("Error while setting bar shift")
        exit(1)

    return bar_shift

def set_pattern_color(type_obj, pos):

    pattern_color = ""

    if type_obj == JAVA:
        if pos == "bot":
            pattern_color = "brown"
        elif pos == "mid":
            pattern_color = "orange"
        elif pos == "top":
            pattern_color = "red"
    elif type_obj == DEG:
        if pos == "bot":
            pattern_color = "brown!60!white"
        elif pos == "mid":
            pattern_color = "orange!60!white"
        elif pos == "top":
            pattern_color = "red!60!white"

    if pattern_color == "":
        print("Error while setting pattern_color")
        exit(1)

    return pattern_color

def set_pattern(type_obj):

    pattern = ""

    if type_obj == JAVA:
        pattern = "crosshatch"
    elif type_obj == DEG:
        pattern = "vertical lines"

    if pattern == "":
        print("Error while setting pattern")
        exit(1)

    return pattern

def set_coordinates(list_perf, list_nbThread):
    coordinates = ""

    if len(list_perf) == len(list_nbThread):
        for i in range(len(list_perf)):
            coordinates += f"""({list_perf[i]},{list_nbThread[i]}) """

    if coordinates == "":
        print("Error while setting coordinates")
        exit(1)

    return coordinates

def generate_groupplot(list_nbThread):

    str_list_nbThread = ""

    for nbThread in list_nbThread:
        str_list_nbThread += str(nbThread)+","

    groupplot = f"""
    \\nextgroupplot[ legend style = {{column sep = 10pt, legend columns = 2, legend to name = grouplegend,}}, symbolic y coords={{{str_list_nbThread}}}, xtick={{0,5,10,15}},xticklabels={{$0$,$5$,$10$,$15$}}, extra x ticks={{1}}, extra x tick style={{grid=major}}, extra x tick labels={{1}}, grid style={{black}}]
    """

    return groupplot

def generate_plot(type_obj, nb_user, list_perf, list_nbThread, pos):

    bar_shift = set_bar_shift(type_obj, pos)
    pattern_color = set_pattern_color(type_obj, pos)
    pattern = set_pattern(type_obj)
    coordinates = set_coordinates(list_perf, list_nbThread)

    plot = f"""
        \\addplot +[bar shift={bar_shift}, pattern color={pattern_color}, draw=black!70, pattern={pattern}] coordinates{{{coordinates}}}; \\addlegendentry{{${"{:.1e}".format(int(nb_user))}$users({type_obj})}}
    """

    return plot

def get_param(figure_type, xtick_label, xtick, ytick_label, ytick, xlabel, ylabel, dap):

    plot_param = ""

    if dap:
        cycle_list = f"""
        {{ 
        {{pattern color=orange!100!white, fill=black!100!white}},
        {{pattern color=black!100!white, fill=black!80!white, pattern=crosshatch}},
        {{pattern color=black!100!white, fill=black!60!white, pattern=north east lines}},
        {{pattern color=black!100!white, fill=black!40!white}}
    }}
        """
    else:
        cycle_list = f"""
        {{ 
        {{pattern color=orange!100!white, fill=orange!100!white}},
        {{pattern color=orange!100!white, fill=orange!80!white, pattern=crosshatch}},
        {{pattern color=orange!100!white, fill=orange!60!white, pattern=north east lines}},
        {{pattern color=orange!100!white, fill=orange!40!white}}
    }}
        """
    if figure_type == 1:
        plot_param = f"""[
    ybar,
    bar width=0.02\\linewidth,
    ylabel={{{ylabel}}},
    ytick={ytick},
    ymin=0,
    ymax=3,
    xlabel={{{xlabel}}},
    xticklabels={{{xtick_label}}},
    xtick={{{xtick[:-1]}}},
    scale only axis,
    width=0.9\\linewidth,
    height = 3cm,
    ymajorgrids,
    axis x line*=bottom,
    axis y line*=left,
    legend style={{draw=none, at={{(0.5,1.2)}}, anchor=north}},
    cycle list={cycle_list},
    area legend,
    ylabel style={{font=\\footnotesize}},
    xlabel style={{font=\\footnotesize}},
    title style={{font=\\footnotesize}},
    tick label style={{font=\\footnotesize}}
]
        """
    elif figure_type == 2:
        plot_param = f"""[
        xbar,
        xlabel={{{xlabel}}},
        xtick={{{xtick}}},
        xticklabels={{{xtick_label}}},
        xmin=0,
        xmax=10000,
        height=0.9\\textwidth,
        bar width=0.06\\textwidth,
        width=\\textwidth,
        ylabel={{{ylabel}}},
        yticklabels={{{ytick_label[:-1]}}},
        ytick={{{ytick[:-1]}}},
        scaled ticks=false,
        enlarge y limits=0.2,
        scale only axis,
        axis x line*=bottom,
        axis y line*=left,
        legend style={{draw=none, at={{(1.1,0.45)}}, anchor=north}},
        cycle list={{ {{fill=MyRoyalBlue!90!white}},{{fill=black}}, {{fill=orange}} }},
        area legend,]
        """
    elif figure_type == 3:
        plot_param = f"""[
        stack plots=y,
        area style,
        axis x line*=bottom,
        axis y line*=left,
        enlarge x limits=false,
        ylabel={{{ylabel}}},
        ytick={ytick},
        ymin=0,
        ymax=10000,
        xlabel={{{xlabel}}},
        xticklabels={{{xtick_label}}},
        xtick={{{xtick[:-1]}}},
        % enlarge x limits=0.2,
        scale only axis,
        width=\\textwidth,
        height = 4cm,
        ymajorgrids,
        axis x line*=bottom,
        axis y line*=left,
        legend style={{draw=none, at={{(0.5,1.2)}}, anchor=north}},
        cycle list={{ {{fill=yellow}},{{fill=orange}}, {{fill=brown}}, {{fill=red}} }},
        area legend,]
        """

    return plot_param

def write_histogram(dico_perf, figure_type):
    list_typeObj = get_list_typeObj(dico_perf)
    list_alpha = get_list_alpha(dico_perf)
    list_nbUsers = get_list_nbUser(dico_perf)
    list_nbThread = get_list_nbThread(dico_perf)

    xticklabels = ""
    xtick = ""
    xlabel = ""
    yticklabels = ""
    ytick = ""
    ylabel = ""

    if figure_type == 1:
        xticklabels = get_str_list_xtick(list_nbThread)
        xticklabels += "Avg"
        xticklabels += ",DAP"
        xtick = get_str_list_xtick(range(len(list_nbThread)+2))
        xlabel = "\\# threads"
        ytick = "{0, 0.5, 1, 1.5, 2}"
        ylabel = "SpeedUp"
    elif figure_type == 2:
        yticklabels = get_str_list_xtick(list_alpha)
        xticklabels = "2, 4, 6, 8, 10"
        ytick = get_str_list_xtick(range(len(list_alpha)))
        ylabel = "$\\alpha$"
        xtick = "2000, 4000, 6000, 8000, 10000"
        xlabel = "Throughput (Mops/s)"
    elif figure_type == 3:
        xticklabels = get_str_list_xtick(list_nbThread)
        xtick = get_str_list_xtick(range(len(list_nbThread)))
        xlabel = "\\# threads"
        ytick = "{1000,2000,3000,4000,5000,6000,7000,8000,9000,10000,11000,12000,13000,14000,15000}"
        ylabel = "Throughput"
    else:
        print("Figure type should be 1,2 or 3")
        exit(1)

    plot_param = get_param(figure_type, xticklabels, xtick, yticklabels, ytick, xlabel, ylabel, False)

    with open(args.output_file, 'w') as out:
        out.write("\\begin{tikzpicture}\n")
        out.write("\\begin{axis}")
        out.write(plot_param)
        legend = "\\legend{"
        x_dashed = len(list_nbThread) - 1 + 0.5
        if figure_type == 1:
            for nbUser in list_nbUsers:
                list_val_deg = list()
                list_val_java = list()
                list_margin = list()
                list_val_speed_up = list()
                i = 0
                plot = "\n\\addplot+[nodes near coords, point meta=explicit symbolic, every node near coord/.append style={rotate=45, anchor=west, yshift=6pt, font=\\footnotesize}, error bars/.cd, y dir=both, y explicit] "
                coordinates = "coordinates {\n"

                for nbThread in list_nbThread:
                    perf_value_deg, margin_deg = get_perf_values(dico_perf[DEG][DEFAULT_ALPHA][nbUser][nbThread])
                    perf_value_java, _ = get_perf_values(dico_perf[JAVA][DEFAULT_ALPHA][nbUser][nbThread])
                    list_val_deg.append(str(perf_value_deg))
                    list_val_java.append(str(perf_value_java))
                    list_margin.append(margin_deg)
                    val, margin = compute_speedup(perf_value_java, perf_value_deg, margin_deg)
                    list_val_speed_up.append(str(val))
                    if nbThread == "1":
                        if nbUser == "100000":
                            coordinates += f""" ({str(i)},{val}) +- ({margin},{margin}) [100K]\n"""
                        elif nbUser == "500000":
                            coordinates += f""" ({str(i)},{val}) +- ({margin},{margin}) [500K]\n"""
                        elif nbUser == "1000000":
                            coordinates += f""" ({str(i)},{val}) +- ({margin},{margin}) [1000K]\n"""
                    else:
                        coordinates += f""" ({str(i)},{val}) +- ({margin},{margin})\n"""
                    i += 1


                val, margin = get_perf_values(list_val_speed_up)
                margin = round(float(statistics.mean(list_margin))/float(perf_value_java),4)
                coordinates += f""" ({str(i)},{val}) +- ({margin},{margin})\n"""
                coordinates += f""" (7,0)\n"""
                coordinates += "};\n"



                plot += coordinates
                out.write(plot)
            out.write(f"""\\draw[thick, black] (axis cs:-1,1) -- (axis cs:8,1);\n""")
            out.write(f"""\\draw[dashed, thick, black] (axis cs:{x_dashed},0) -- (axis cs:{x_dashed},3);\n""")
            out.write("\\end{axis}\n\n")

            out.write("\\begin{axis}")
            plot_param = get_param(figure_type, xticklabels, xtick, yticklabels, ytick, xlabel, ylabel, True)
            plot_param = plot_param.strip()
            plot_param = plot_param[:-2] + ",\n    yticklabel={\\empty},\n    xticklabel={\\empty}\n]"
            plot_param.replace("SpeedUp","\\empty")
            plot_param.replace("\\# threads","\\empty")
            out.write(plot_param)

            for nbUser in list_nbUsers:
                list_val_dap = list()
                list_val_java = list()
                list_margin_dap = list()
                list_val_speed_up = list()

                plot = "\n\\addplot+[nodes near coords, point meta=explicit symbolic, every node near coord/.append style={rotate=45, anchor=west, yshift=6pt, font=\\footnotesize}, error bars/.cd, y dir=both, y explicit] "
                coordinates = "coordinates {\n"
                i = 0

                for nbThread in list_nbThread:
                    perf_value_dap, margin_dap = get_perf_values(dico_perf[DAP][DEFAULT_ALPHA][nbUser][nbThread])
                    perf_value_java, _ = get_perf_values(dico_perf[JAVA][DEFAULT_ALPHA][nbUser][nbThread])
                    list_val_dap.append(str(perf_value_dap))
                    list_val_java.append(str(perf_value_java))
                    list_margin_dap.append(margin_dap)
                    val, margin = compute_speedup(perf_value_java, perf_value_dap, margin_dap)
                    list_val_speed_up.append(val)
                    coordinates += f""" ({str(i)},0)\n"""
                    i += 1


                val, margin = get_perf_values(list_val_speed_up)
                margin = round(float(statistics.mean(list_margin_dap))/float(perf_value_java),4)
                coordinates += f""" (6,0)\n"""
                coordinates += f""" (7,{val}) +- ({margin},{margin})\n"""
                coordinates += "};\n"
                plot += coordinates
                out.write(plot)
                legend += nbUser + ","

            out.write("\\end{axis}\n")
        elif figure_type == 2:
            for typeObj in list_typeObj:
                i = 0
                plot = "\n\\addplot+[error bars/.cd, x dir=both, x explicit] "
                coordinates = "coordinates {\n"
                for alpha in list_alpha:
                    val, margin = get_perf_values(dico_perf[typeObj][alpha][DEFAULT_NBUSER][DEFAULT_NBTHREAD])
                    coordinates += f""" ({val},{str(i)}) +- ({margin},{margin})\n"""
                    i += 1
                coordinates += "};\n"
                plot += coordinates
                out.write(plot)
                legend += typeObj + ","

            legend = legend[:-1]
            legend += "}\n"
            out.write(legend)
            out.write("\\end{axis}\n")

        elif figure_type == 3:
            dico_last_val = dict()
            for nbThread in list_nbThread:
                dico_last_val.setdefault(nbThread, 0)
            for typeObj in list_typeObj:
                i = 0
                plot = "\n\\addplot "
                coordinates = "coordinates{\n"
                for nbThread in list_nbThread:
                    val, margin = get_perf_values(dico_perf[typeObj][DEFAULT_ALPHA][DEFAULT_NBUSER][nbThread])
                    val = val - dico_last_val[nbThread]
                    coordinates += f""" ({str(i)},{val})\n"""
                    dico_last_val[nbThread] = dico_last_val[nbThread] + val
                    i += 1
                coordinates += "}\n"

                plot += coordinates
                plot+= "\\closedcycle;\n"
                out.write(plot)
                legend += typeObj + ","





        out.write("\\end{tikzpicture}")

        # out.write(legend)
        # out.write("\\end{axis}\n")

def read_perf_file(figure_type):
    dico_perf = dict()

    if figure_type == 1 or figure_type == 2:
        with open(args.perf_file, 'r') as f:
            for line in f:
                parts = line.split(";")

                if len(parts) != 5:
                    print("File should be in format : type;alpha;nbUser;nbThread;throughput")
                    exit(1)

                typeObj = parts[0]
                alpha = parts[1]
                nbUser = parts[2]
                nbThread = parts[3]
                throughput = parts[4]

                dico_perf.setdefault(typeObj, dict())
                dico_perf[typeObj].setdefault(alpha, dict())
                dico_perf[typeObj][alpha].setdefault(nbUser, dict())
                dico_perf[typeObj][alpha][nbUser].setdefault(nbThread, list())
                dico_perf[typeObj][alpha][nbUser][nbThread].append(throughput)

    elif figure_type == 3:
        for perf_file in args.obj_perf_files:
            with open(perf_file, 'r') as f:
                for line in f:
                    parts = line.split(";")

                    if len(parts) != 5:
                        print("File should be in format : type;alpha;nbUser;nbThread;throughput")
                        exit(1)

                    typeObj = os.path.basename(f.name)[:-4]
                    alpha = parts[1]
                    nbUser = parts[2]
                    nbThread = parts[3]
                    throughput = parts[4]

                    dico_perf.setdefault(typeObj, dict())
                    dico_perf[typeObj].setdefault(alpha, dict())
                    dico_perf[typeObj][alpha].setdefault(nbUser, dict())
                    dico_perf[typeObj][alpha][nbUser].setdefault(nbThread, list())
                    dico_perf[typeObj][alpha][nbUser][nbThread].append(throughput)

    return dico_perf



def main():
    figure_type = args.type_figure
    dico_perf = read_perf_file(figure_type)
    write_histogram(dico_perf, figure_type)

if __name__ == "__main__":
    args = setUp_parser()
    main()