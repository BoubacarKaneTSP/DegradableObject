import re
from collections import Counter
import argparse
import os

def create_pie_chart(word_counts, clazz):

    with open(clazz+"_piechart.txt", 'w') as out:
        out.write("\\begin{tikzpicture}\n")
        out.write("\\def\\printonlylargeenough#1#2{\\unless\\ifdim#2pt<#1pt\\relax\n")
        out.write("#2\\printnumbertrue\n")
        out.write("\\else\n")
        out.write("\\printnumberfalse\n")
        out.write("\\fi}\n")
        out.write("\\newif\\ifprintnumber\n")
        out.write("\\pie[radius=4,before number=\\printonlylargeenough{5}, after number=\\ifprintnumber\\%\\fi]{\n")
        for word, count in word_counts.items():
            if count >= 5 :
                out.write(str(count)+"/"+word+", ")
            else:
                out.write(str(count)+"/, ")
        out.write("}\n")
        out.write("\\end{tikzpicture}\n")

def count_word_occurrences(file_path):
    with open(file_path, 'r', encoding='utf-8') as file:
        content = file.read()

    clazz = os.path.basename(file_path)[:-4]

    words = re.findall(r'\b\+?(\w+)\b', content.lower())  # Extraction des mots sans le symbole +

    word_counts = Counter(words)

    total_words = sum(word_counts.values())

    raw_percentages = {word: round((count / total_words) * 100, 2) for word, count in word_counts.most_common()}

    count_others = 0
    sumup_words = dict()

    for word, count in raw_percentages.items():
        if count >= 1:
            sumup_words[word] = count
        else:
            count_others += count

    sumup_words["autres"] = round(count_others,2)

    # rounded_percentages = {word: round(percentage) for word, percentage in raw_percentages.items()}

    # total_rounded = sum(rounded_percentages.values())
    # difference = 100 - total_rounded

    # if difference != 0:
    #     sorted_words = sorted(raw_percentages, key=lambda word: raw_percentages[word] - rounded_percentages[word], reverse=True)
    #     for i in range(abs(difference)):
    #         word_to_adjust = sorted_words[i]
    #         rounded_percentages[word_to_adjust] += (1 if difference > 0 else -1)

    create_pie_chart(sumup_words, clazz)

# Lecture des arguments de la ligne de commande
if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Count occurrences of each word in a file.")
    parser.add_argument("-f", dest="file_path", help="The path to the file to analyze.")

    args = parser.parse_args()

    count_word_occurrences(args.file_path)
