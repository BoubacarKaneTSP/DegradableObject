import os
import argparse

def setUp_parser():
    parser = argparse.ArgumentParser(description='Generate a latex graph from the values in the files')
    parser.add_argument('-a', '--average', dest='average', type=str, help='Average number of declaration file')
    parser.add_argument('-p', '--proportion', dest='proportion', type=str, help='Proportion of declaration file')
    # parser.add_argument('-f', '--files', dest='files', action="append", type=str, help='Files')
    # parser.add_argument('-l', '--label', dest='label', action='store_false', help='Add the y label for the first plot')

    args = parser.parse_args()

    return args

def generate_latex_file(file_name_avg, file_name_proportion):

    years=("2015", "2018", "2021", "2024")


    with open('results/evolution.tex', 'w') as f:
        f.write("\\begin{axis}[\n")
        f.write("    ybar,\n")
        f.write("    bar width=0.05\\linewidth,\n")
        f.write("    width=0.8\\textwidth,\n")
        f.write("    height=0.8\\textwidth,\n")
        f.write("    xlabel={Years},\n")
        f.write("    x label style={anchor=north, below=1mm},\n")
        f.write("    ylabel={\#~Declarations},\n")
        f.write("    y label style={font=\\footnotesize},\n")
        f.write("    ymin=0,\n")
        f.write("    ymax=150,\n")
        f.write("    ytick={0, 25, 50, 75, 100, 125, 150},\n")
        f.write("    y tick label style={font=\\footnotesize},\n")
        f.write("    xtick={2015,2018,2021,2024},\n")
        f.write("    legend style={at={(0.5,-0.25)}, anchor=north, legend columns=2},\n")
        f.write("    x label style={anchor=south,yshift=-2em,font=\\footnotesize},\n")
        f.write("    axis y line*=left,\n")
        f.write("    axis x line*=bottom,\n")
        f.write("    x tick label style={/pgf/number format/1000 sep=, rotate=45,anchor=east,font=\\footnotesize},\n")
        f.write("    every node near coord/.style={/pgf/number format/1000 sep=, rotate=45, yshift=-0.2em, xshift=0.6em, font=\\tiny},\n")
        f.write("    nodes near coords\n]\n")

        f.write("    \\addplot[fill=orange!50!white,draw=MyRoyalBlue,] coordinates {\n")
        try:
            with open(file_name_avg, 'r') as file:
                #with open(os.path.basename(filename), 'r') as file:
                for line in file:
                    try:
                        x, y = line.strip().split()
                    except ValueError:
                        print(file_name_avg)
                        print(line.strip().split())

                    if x in years:
                        f.write(f"       ({x}, {y})\n")
        except FileNotFoundError:
            print(f"File not found : {file_name_avg}")

        f.write("   };\n\n")
        f.write("\\end{axis}\n\n")

        f.write("\\begin{axis}[\n")
        f.write("    width=0.8\\textwidth,\n")
        f.write("    height=0.8\\textwidth,\n")
        f.write("    ymin=0.5,\n")
        f.write("    ymax=1,\n")
        f.write("    ytick={0.5, 0.6, 0.7, 0.8, 0.9, 1},\n")
        f.write("    y tick label style={font=\\footnotesize},\n")
        f.write("    axis y line*=right,\n")
        f.write("    ylabel near ticks,\n")
        f.write("    yticklabel pos=right,\n")
        f.write("    ylabel={Proportion (\\%)},\n")
        f.write("    axis x line=none,\n")
        f.write("    y label style={anchor=south, yshift=-2em, font=\\footnotesize},\n")
        f.write("    legend style={at={(0,-0.35)}, anchor=north, legend columns=2}\n]\n\n")

        f.write("    \\addplot[MyRoyalBlue, thick, mark=*] coordinates {\n")
        try:
            with open(file_name_proportion, 'r') as file:
                for line in file:
                    try:
                        x, y = line.strip().split()
                    except ValueError:
                        print(file_name_proportion)
                        print(line.strip().split())

                    if x in years:
                        f.write(f"       ({x}, {y})\n")
        except FileNotFoundError:
            print(f"File not found : {file_name_proportion}")

        f.write("   };\n\n")
        f.write("\\end{axis}\n")
        
args = setUp_parser()

generate_latex_file(args.average, args.proportion)
