import matplotlib.pyplot as plt
import sys

# Vérifier si le nom du fichier et le nombre de bins sont fournis en arguments
def plot_word_histogram(file_path):
    word_count_pairs = []

    with open(file_path, 'r') as file:

        for line in file:
            word, count = line.strip().split()
            count = int(count)
            # print(count)
            # if count > 0:
            word_count_pairs.append((word, count))

    word_count_pairs.sort(key=lambda pair: pair[1], reverse=True)

    words = [pair[0] for pair in word_count_pairs]
    counts = [pair[1] for pair in word_count_pairs]

    dico = {i: 0 for i in range(max(counts) + 1)}

    for count in counts:
        dico[count] = dico[count] + 1

    print(len(words))
    plt.bar(dico.keys(), dico.values())
    plt.xlabel('Mots')
    plt.ylabel('Entier associé')
    plt.title('Histogramme ')
    plt.xticks(rotation=45, ha='right')

    plt.tight_layout()
    plt.show()

if len(sys.argv) < 2:
    print("Veuillez fournir le nom du fichier en arguments.")
    print("Exemple: python histogram.py fichier.txt")
    sys.exit(1)

# Récupérer le nom du fichier et le nombre de bins à partir des arguments de ligne de commande
file_path = sys.argv[1]

plot_word_histogram(file_path)
