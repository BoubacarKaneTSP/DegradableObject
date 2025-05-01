import os
from collections import Counter

# List of types and associated filenames
types = [
    "AtomicLong",
    "ConcurrentHashMap",
    "ConcurrentSkipListSet",
    "ConcurrentLinkedQueue"
]

def process_file(filename):
    with open(filename, 'r') as f:
        content = f.read()
    # Remove leading "+" if any, and split by spaces
    methods = [method.lstrip('+') for method in content.split()]
    counter = Counter(methods)
    return counter

def generate_pie_entries(counter):
    total_invocations = sum(counter.values())
    most_common = counter.most_common(3)

    top_methods = set(method for method, _ in most_common)
    other_methods = [method for method in counter if method not in top_methods]
    other_invocations = sum(counter[method] for method in other_methods)

    entries = []
    for method, count in most_common:
        proportion = (count / total_invocations) * 100
        entries.append(f"{proportion:.1f}/\\normalsize {method}")

    if other_methods:
        proportion = (other_invocations / total_invocations) * 100
        entries.append(
            f"{proportion:.1f}/\\normalsize \\textit{{\\shortstack[c]{{others\\\\({len(other_methods)})}}}}"
        )

    entries_str = ", ".join(entries)
    return entries_str

def generate_tikz_picture(pie_entries, typename, rotate_angle=115):
    tikz = f"""
  \\begin{{tikzpicture}}[scale=0.8]
    \\def\\printonlylargeenough#1#2{{\\unless\\ifdim#2pt<#1pt\\relax
      #2\\printnumbertrue
      \\else
      \\printnumberfalse
      \\fi}}
    \\newif\\ifprintnumber
    \\pie[rotate={rotate_angle}, radius=3, before number=\\printonlylargeenough{{10}}, after number=\\ifprintnumber\\%\\fi]{{{pie_entries}}}
    \\node () at (-.2,-4) {{\\bf\\normalsize\\code{{{typename}}}}};
  \\end{{tikzpicture}}
"""
    return tikz

def main():
    rotate_angles = {   # Optional: assign different rotation angles if needed
        "ConcurrentHashMap": 120,
        "ConcurrentSkipListSet": 120,
        "ConcurrentLinkedQueue": 100,
        "AtomicLong": 115
    }

    tikz_blocks = []

    for typename in types:
        filename = f"{typename}.txt"
        if os.path.exists(filename):
            counter = process_file(filename)
            pie_entries = generate_pie_entries(counter)
            rotate = rotate_angles.get(typename, 115)
            tikz_code = generate_tikz_picture(pie_entries, typename, rotate)
            tikz_blocks.append(tikz_code)
            print(f"Processed {typename}")
        else:
            print(f"Warning: {filename} not found.")

    # Assemble the final tabular environment
    output = "\\begin{tabular}{@{}l@{~~}l@{}}\n"
    output += tikz_blocks[1] + "\n  &\n" + tikz_blocks[2] + "\\\\\n"
    output += tikz_blocks[3] + "\n  &\n" + tikz_blocks[0] + "\n"
    output += "\\end{tabular}\n"

    with open("results/all_pie_charts.tex", "w") as f:
        f.write(output)

    print("Generated all_pie_charts.tex")

if __name__ == "__main__":
    main()
