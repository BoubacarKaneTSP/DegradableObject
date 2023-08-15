import matplotlib.pyplot as plt
import sys

# Vérifier si le nom du fichier et le nombre de bins sont fournis en arguments
def plot_word_histogram(file_path):
    word_count_pairs = []

    with open(file_path, 'r') as file:

        for line in file:
            word, count = line.strip().split()
            count = int(count)
            if count > 0:
                word_count_pairs.append((word, count))

    word_count_pairs.sort(key=lambda pair: pair[1], reverse=True)

    words = [pair[0] for pair in word_count_pairs]
    counts = [pair[1] for pair in word_count_pairs]

    dico = {i: 0 for i in range(max(counts) + 1)}

    bin_size = 100
    for count in counts:
        key = count - count % bin_size #puting number of calls in a span of 5
        dico[key] = dico[key] + 1

    key_to_remove = []
    for key in dico.keys():
        if dico[key] == 0:
            key_to_remove.append(key)

    for key in key_to_remove:
        del dico[key]

    nb_call_total = 0

    for key in dico.keys():
        nb_call_total += dico[key] * key


    dico_proportion = {}

    for key in dico.keys():
        dico_proportion[key] = ((dico[key]* max(key,1)) / nb_call_total) * 100

    print("nb call total : ", nb_call_total)
    nb_call = dico.keys()
    nb_user = dico.values()
    print("dico : ",dico,"\n")
    print("dico proportion : ",dico_proportion,"\n")
    # print(len(words))

    nb_call = sorted(nb_call, reverse=True)
    nb_user = sorted(nb_user)

    # nb_call = nb_call[:20]
    # nb_user = nb_user[:20]

    print("nb call : ", nb_call,"\n")

    print("nb user : ", nb_user,"\n")
    # plt.bar(nb_call, nb_user, align='center')
    plt.bar(range(len(nb_call)), nb_user, tick_label=nb_call)

    plt.xlabel('Number of Call')
    plt.ylabel('Number of Users')
    plt.title('Distribution of Users by Number of Call')
    plt.xticks(rotation=45, ha='right')
    plt.yscale("log")

    plt.show()

    # x = range(len(nb_call))
    #
    # plt.bar(x, nb_call, width=0.4, align='center', label='nb call')
    # plt.bar([i+0.4 for i in x], nb_user, width=0.4, align='center', label='nb user')
    #
    # plt.xlabel('Index')
    # plt.ylabel('Count')
    # plt.title('Comparison of nb call and nb user')
    # plt.xticks([i+0.2 for i in x], x)
    # plt.legend()
    # plt.tight_layout()
    #
    # plt.show()

    # plt.bar(nb_call, nb_user)
    # plt.xlabel('# of call')
    # plt.ylabel('# of user')
    #
    # print("nb call : ", nb_call)
    # print("nb user : ", nb_user)
    # plt.bar(nb_user, nb_call)
    # plt.xlabel('# of user')
    # plt.ylabel('# of call')

    # plt.title('Number of user called X times')

    # plt.tight_layout()
    # plt.show()

if len(sys.argv) < 2:
    print("Veuillez fournir le nom du fichier en arguments.")
    print("Exemple: python histogram.py fichier.txt")
    sys.exit(1)

# Récupérer le nom du fichier et le nombre de bins à partir des arguments de ligne de commande
file_path = sys.argv[1]

plot_word_histogram(file_path)
