import matplotlib.pyplot as plt
from collections import Counter
import re
import sys


def plot_word_histogram(file_path):
    # Lecture du contenu du fichier
    with open(file_path, 'r') as file:
        text = file.read()

    # Séparation du texte en mots en supprimant la ponctuation et en mettant en minuscules
    words = re.findall(r'\b\w+\b', text.lower())

    # Comptage de la fréquence des mots
    word_count = Counter(words)

    # Filtrage des mots apparaissant plus de 3 fois
    frequent_words = {word: count for word, count in word_count.items() if count > 3}

    # Tri des mots en fonction de leur fréquence
    sorted_words = sorted(frequent_words, key=frequent_words.get, reverse=True)
    sorted_counts = [frequent_words[word] for word in sorted_words]

    # Création de l'histogramme
    plt.bar(sorted_words, sorted_counts)
    plt.xlabel('Mots')
    plt.ylabel('Fréquence')
    plt.title('Distribution des mots dans le fichier (triés)')
    plt.xticks(rotation=45, ha='right')

    # Affichage de l'histogramme
    plt.tight_layout()
    plt.show()

# Vérifier si le nom du fichier et le nombre de bins sont fournis en arguments
if len(sys.argv) < 2:
    print("Veuillez fournir le nom du fichier en arguments.")
    print("Exemple: python histogram.py fichier.txt")
    sys.exit(1)

# Récupérer le nom du fichier et le nombre de bins à partir des arguments de ligne de commande
file_path = sys.argv[1]

plot_word_histogram(file_path)