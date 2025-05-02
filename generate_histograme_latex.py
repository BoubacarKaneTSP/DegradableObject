import os
import argparse

def setUp_parser():
    parser = argparse.ArgumentParser(description='Generate a latex graph from the values in the files')
    parser.add_argument('-u', '--updateRate', dest='update_rate', default="", type=str, help='Update rates')
    parser.add_argument('-f', '--files', dest='files', action="append", type=str, help='Files')

    args = parser.parse_args()

    return args

def generate_latex(file1, file2, file3, file4, output_file):
    try:
        with open(file1, 'r') as f1, open(file2, 'r') as f2, open(file3, 'r') as f3, open(file4, 'r') as f4, open(output_file, 'w') as out:

            obj_name1 = os.path.basename(f1.name)[:-4]
            obj_name1 = obj_name1.split("_")[0]
            obj_name2 = os.path.basename(f2.name)[:-4]
            obj_name2 = obj_name2.split("_")[0]

            out.write("\\begin{axis}[\n")
            out.write("    ybar,\n")
            out.write("    width=0.6\\linewidth,\n")
            out.write("    height=0.6\\textwidth,\n")
            out.write("    bar width=0.03\\linewidth,\n")
            if args.update_rate == "25":
                out.write("    ylabel={\\shortstack{25\\% upd \\\\ Kops/s per thread}},\n")
            elif args.update_rate == "75":
                out.write("    ylabel={\\shortstack{75\\% upd \\\\ Kops/s per thread}},\n")
            else:
                out.write("    ylabel={"+args.update_rate+"\\% upd},\n")
                out.write("    yticklabel={\\empty},\n")

            out.write("    ytick={0, 1.0E4, 2.0E4, 3.0E4, 4.0E4, 5.0E4, 6.0E4, 7.0E4, 8.0E4, 9.0E4, 1.0E5},\n")
            out.write("    ymin=0,\n")
            out.write("    ymax=100000,\n")
            out.write("    symbolic x coords={LeftMargin, Unordered, Space, Ordered, RightMargin},\n")
            out.write("    ymajorgrids,\n")
            out.write("    axis x line*=bottom,\n")
            out.write("    axis y line*=left,\n")
            out.write("    xtick=data,\n")
            out.write("    legend style={draw=none, at={(0.8,1.3)}, anchor=north, font=\\normalsize},\n")
            out.write("    xmin=LeftMargin,\n")
            out.write("    xmax=RightMargin,\n")
            out.write("    ylabel style={font=\\normalsize},\n")
            out.write("    xlabel style={font=\\normalsize},\n")
            out.write("    title style={font=\\normalsize},\n")
            out.write("    tick label style={font=\\normalsize},\n")
            out.write("    cycle list={ {fill=orange}, {fill=orange} },\n")
            out.write("    area legend,\n")
            out.write("]\n")

            nThreads = list()
            for line1, line2 in zip(f1, f2):
                if "AVG" in line1:
                    continue

                parts1 = line1.strip().split()
                parts2 = line2.strip().split()
                assert parts1[0] == parts2[0], "The number of thread should be identical for each line"
                int_nThread = int(parts1[0])

                try:
                    int_mean1 = int(parts1[1])
                    int_upper_bound1 = int(parts1[4])
                    int_lower_bound1 = int(parts1[5])

                    lower_margin1 = abs(int_mean1 - int_lower_bound1)
                    upper_margin1 = abs(int_mean1 - int_upper_bound1)

                    int_mean2 = int(parts2[1])
                    int_upper_bound2 = int(parts2[4])
                    int_lower_bound2 = int(parts2[5])
                except IndexError:
                    print("Perhaps the throughput file are not in detailed mode")
                    exit(1)

                lower_margin2 = abs(int_mean2 - int_lower_bound2)
                upper_margin2 = abs(int_mean2 - int_upper_bound2)

                nThreads.append(int_nThread)

                out.write("    \\addplot+[error bars/.cd, y dir=both, y explicit] coordinates {\n        (Unordered," + parts1[1] + ") +- ("+str(lower_margin1)+", "+str(upper_margin1)+") \n        (Ordered," + parts2[1] + ") +-("+str(lower_margin2)+", "+str(upper_margin2)+")\n    };\n\n")

            out.write("\\end{axis}\n\n")


            obj_name3 = os.path.basename(f3.name)[:-4]
            obj_name3 = obj_name1.split("_")[0]
            obj_name4 = os.path.basename(f4.name)[:-4]
            obj_name4 = obj_name2.split("_")[0]

            out.write("\\begin{axis}[\n")
            out.write("    ybar,\n")
            out.write("    width=0.6\\linewidth,\n")
            out.write("    height=0.6\\textwidth,\n")
            out.write("    bar width=0.03\\linewidth,\n")
            out.write("    ymin=0,\n")
            out.write("    ymax=100000,\n")
            out.write("    symbolic x coords={LeftMargin, Unordered, Space, Ordered, RightMargin},\n")
            out.write("    ymajorgrids,\n")
            out.write("    axis x line*=bottom,\n")
            out.write("    axis y line*=left,\n")
            out.write("    xtick=data,\n")
            out.write("    legend style={draw=none, at={(0.9,1.2)}, anchor=north, font=\\normalsize},\n")
            out.write("    xmin=LeftMargin,\n")
            out.write("    xmax=RightMargin,\n")
            out.write("    ylabel style={font=\\normalsize},\n")
            out.write("    xlabel style={font=\\normalsize},\n")
            out.write("    title style={font=\\normalsize},\n")
            out.write("    tick label style={font=\\normalsize},\n")
            out.write("    cycle list={ {fill=MyRoyalBlue}, {fill=MyRoyalBlue} },\n")
            out.write("    area legend,\n")
            out.write("    yticklabel={\\empty},\n")
            out.write("    xticklabel={\\empty},\n")
            out.write("    scaled y ticks=false,\n")
            out.write("]\n")

            nThreads = list()
            for line1, line2 in zip(f3, f4):
                if "AVG" in line1:
                    continue

                parts1 = line1.strip().split()
                parts2 = line2.strip().split()
                assert parts1[0] == parts2[0], "The number of thread should be identical for each line"
                int_nThread = int(parts1[0])

                try:
                    int_mean1 = int(parts1[1])
                    int_upper_bound1 = int(parts1[4])
                    int_lower_bound1 = int(parts1[5])

                    lower_margin1 = abs(int_mean1 - int_lower_bound1)
                    upper_margin1 = abs(int_mean1 - int_upper_bound1)

                    int_mean2 = int(parts2[1])
                    int_upper_bound2 = int(parts2[4])
                    int_lower_bound2 = int(parts2[5])
                except IndexError:
                    print("Perhaps the throughput file are not in detailed mode")
                    exit(1)

                lower_margin2 = abs(int_mean2 - int_lower_bound2)
                upper_margin2 = abs(int_mean2 - int_upper_bound2)

                nThreads.append(int_nThread)

                out.write("    \\addplot+[error bars/.cd, y dir=both, y explicit] coordinates {\n        (Unordered," + parts1[1] + ") +- ("+str(lower_margin1)+", "+str(upper_margin1)+") \n        (Ordered," + parts2[1] + ") +-("+str(lower_margin2)+", "+str(upper_margin2)+")\n    };\n\n")

            out.write("\\end{axis}")
    except FileNotFoundError:
        print(f"File not found : {file1}, {file2}, {file3} or {file4}")

args = setUp_parser()

update_rate = args.update_rate

if len(args.update_rate) != 0:
    update_rate += "/"

for i in range(len(args.files)):
    args.files[i] = "microbenchmark_results/avg_perf/" + update_rate +"/"+ args.files[i]

generate_latex(args.files[0], args.files[1], args.files[2], args.files[3], "results/histogram_ratio_"+ args.update_rate +".tex")
