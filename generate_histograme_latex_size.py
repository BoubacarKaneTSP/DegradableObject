import os
import argparse

def setUp_parser():
    parser = argparse.ArgumentParser(description='Generate a latex graph from the values in the files')
    parser.add_argument('-s', '--sizes', dest='sizes', action="append", type=str, help='Differents sizes')
    parser.add_argument('-od', '--filesdeg', dest='deg_obj', type=str, help='Degraded object name')
    parser.add_argument('-oj', '--filesjava', dest='java_obj', type=str, help='Java object name')

    args = parser.parse_args()

    return args

def generate_latex(deg_files, java_files):
    try:
        with open(deg_files[0], 'r') as df1, open(deg_files[1], 'r') as df2, open(deg_files[2], 'r') as df3, open(java_files[0], 'r') as jf1, open(java_files[1], 'r') as jf2, open(java_files[2], 'r') as jf3, open("results/sizes.tex", 'w') as out:

            out.write("\\begin{axis}[\n")
            out.write("    width=\\linewidth,\n")
            out.write("    height=0.5\\textwidth,\n")
            out.write("    bar width=0.03\\linewidth,\n")
            out.write("    ybar,\n")
            out.write("    ylabel={Kop/s per process},\n")
            out.write("    ytick={0, 1.0E4, 2.0E4, 3.0E4, 4.0E4, 5.0E4, 6.0E4, 7.0E4, 8.0E4, 9.0E4, 1.0E5},\n")
            out.write("    ymin=0,\n")
            out.write("    ymax=100000,\n")
            out.write("    ymajorgrids,\n")
            out.write("    axis x line*=bottom,\n")
            out.write("    axis y line*=left,\n")
            out.write("    xtick={0,1,2},\n")
            out.write("    xticklabels={16K,32K,64K},\n")
            out.write("    legend style={draw=none, at={(0.8,1.05)}, anchor=north, font=\\normalsize},\n")
            out.write("    enlarge x limits=0.3,\n")
            out.write("    cycle list={ {fill=orange}, {fill=orange} },\n")
            out.write("    area legend,\n")
            out.write("    ylabel style={font=\\normalsize},\n")
            out.write("    xlabel style={font=\\normalsize},\n")
            out.write("    title style={font=\\normalsize},\n")
            out.write("    y tick label style={font=\\normalsize},\n")
            out.write("    x tick label style={font=\\normalsize},\n")
            out.write("]\n")

            for line1, line2, line3 in zip(df1, df2, df3):
                if "AVG" in line1:
                    continue

                parts = list()

                parts.append(line1.strip().split())
                parts.append(line2.strip().split())
                parts.append(line3.strip().split())
                assert parts[0][0] == parts[1][0] and parts[1][0] == parts[2][0], "The number of thread should be identical for each line"

                int_mean = list()
                int_upper_bound = list()
                int_lower_bound = list()
                upper_margin = list()
                lower_margin = list()

                try:
                    for i in range(len(parts)):
                        int_mean.append(int(parts[i][1]))
                        int_upper_bound.append(int(parts[i][4]))
                        int_lower_bound.append(int(parts[i][5]))
                        lower_margin.append(abs(int_mean[i] - int_lower_bound[i]))
                        upper_margin.append(abs(int_mean[i] - int_upper_bound[i]))

                except IndexError:
                    print("Perhaps the throughput file are not in detailed mode")
                    exit(1)

                out.write("\\addplot+[nodes near coords, point meta=explicit symbolic, every node near coord/.append style={yshift=6pt, xshift=-0.1cm, rotate=45, anchor=west, font=\\normalsize}, error bars/.cd, y dir=both, y explicit] coordinates {\n (0," + str(int_mean[0]) + ") +- ("+str(lower_margin[0])+", "+str(upper_margin[0])+") \n (1," + str(int_mean[1]) + ") +- ("+str(lower_margin[1])+", "+str(upper_margin[1])+") \n (2," + str(int_mean[2]) + ") +- ("+str(lower_margin[2])+", "+str(upper_margin[2])+") \n};\n")

            out.write("\\legend{DEG}\n")
            out.write("\\end{axis}\n\n")

            out.write("\\begin{axis}[\n")
            out.write("    width=\\linewidth,\n")
            out.write("    height=0.5\\textwidth,\n")
            out.write("    bar width=0.03\\linewidth,\n")
            out.write("    ybar,\n")
            out.write("    ytick={0, 1.0E4, 2.0E4, 3.0E4, 4.0E4, 5.0E4, 6.0E4, 7.0E4, 8.0E4, 9.0E4, 1.0E5},\n")
            out.write("    ymin=0,\n")
            out.write("    ymax=100000,\n")
            out.write("    ymajorgrids,\n")
            out.write("    axis lines=none,\n")
            out.write("    axis y line*=left,\n")
            out.write("    xtick={0,1,2},\n")
            out.write("    xticklabels={16K,32K,64K},\n")
            out.write("    legend style={draw=none, at={(0.8,0.88)}, anchor=north, font=\\normalsize},\n")
            out.write("    enlarge x limits=0.3,\n")
            out.write("    cycle list={ {fill=MyRoyalBlue}, {fill=MyRoyalBlue} },\n")
            out.write("    area legend,\n")
            out.write("    ylabel style={font=\\normalsize},\n")
            out.write("    xlabel style={font=\\normalsize},\n")
            out.write("    title style={font=\\normalsize},\n")
            out.write("    tick label style={font=\\normalsize},\n")
            out.write("    yticklabel={\\empty},\n")
            out.write("    xticklabel={\\empty},\n")
            out.write("    scaled y ticks=false,\n")
            out.write("]\n")

            for line1, line2, line3 in zip(jf1, jf2, jf3):
                if "AVG" in line1:
                    continue

                parts = list()

                parts.append(line1.strip().split())
                parts.append(line2.strip().split())
                parts.append(line3.strip().split())
                assert parts[0][0] == parts[1][0] and parts[1][0] == parts[2][0], "The number of thread should be identical for each line"

                int_mean = list()
                int_upper_bound = list()
                int_lower_bound = list()
                upper_margin = list()
                lower_margin = list()

                try:
                    for i in range(len(parts)):
                        int_mean.append(int(parts[i][1]))
                        int_upper_bound.append(int(parts[i][4]))
                        int_lower_bound.append(int(parts[i][5]))
                        lower_margin.append(abs(int_mean[i] - int_lower_bound[i]))
                        upper_margin.append(abs(int_mean[i] - int_upper_bound[i]))

                except IndexError:
                    print("Perhaps the throughput file are not in detailed mode")
                    exit(1)

                out.write("\\addplot+[nodes near coords, point meta=explicit symbolic, every node near coord/.append style={yshift=3pt, xshift=-0.35cm, rotate=45, anchor=west, font=\\normalsize}, error bars/.cd, y dir=both, y explicit]coordinates {\n (0," + str(int_mean[0]) + ") +- ("+str(lower_margin[0])+", "+str(upper_margin[0])+") \n (1," + str(int_mean[1]) + ") +-("+str(lower_margin[1])+", "+str(upper_margin[1])+") \n (2," + str(int_mean[2]) + ") +-("+str(lower_margin[2])+", "+str(upper_margin[2])+") \n};\n")

            out.write("\\legend{JUC}\n")
            out.write("\\end{axis}")
    except FileNotFoundError:
        print(f"File not found : {df1}, {df2}, {df3}, {jf1}, {jf2} or {jf3}")
        exit(1)

args = setUp_parser()

if len(args.sizes) != 3:
    print("Provides 3 differents sizes")
    exit(1)

deg_files=list()
java_files=list()

for size in args.sizes:
    deg_files.append("microbenchmark_results/avg_perf/" + str(size) + "/" + args.deg_obj + "_ALL.txt")
    java_files.append("microbenchmark_results/avg_perf/" + str(size) + "/" + args.java_obj + "_ALL.txt")


generate_latex(deg_files, java_files)
