import os

def sort_and_group_lines_in_directory(input_dir, output_dir):
    # Créer le dossier de sortie s'il n'existe pas
    if not os.path.exists(output_dir):
        os.makedirs(output_dir)

    # Lire les fichiers et compter les lignes dans chaque fichier
    files_with_line_count = []
    for filename in os.listdir(input_dir):
        input_file_path = os.path.join(input_dir, filename)
        
        if os.path.isfile(input_file_path):
            # Compter les lignes dans le fichier
            with open(input_file_path, 'r') as file:
                line_count = sum(1 for _ in file)
            
            files_with_line_count.append((line_count, filename))

    # Trier les fichiers par nombre de lignes (ordre décroissant)
    files_with_line_count.sort(reverse=True, key=lambda x: x[0])

    # Parcourir chaque fichier trié par nombre de lignes
    for line_count, filename in files_with_line_count:
        input_file_path = os.path.join(input_dir, filename)
        output_filename = f"{line_count}_{filename}"  # Nom de sortie avec le nombre de lignes
        output_file_path = os.path.join(output_dir, output_filename)

        positive_lines = []
        negative_lines = []

        # Lire les lignes et les trier en fonction du signe
        with open(input_file_path, 'r') as file:
            for line in file:
                parts = line.strip().split()
                if len(parts) == 3:
                    number, text, sign = parts[0], parts[1], parts[2]
                    if sign == '+':
                        positive_lines.append(line)
                    elif sign == '-':
                        negative_lines.append(line)

        # Écrire les lignes triées dans le fichier de sortie
        with open(output_file_path, 'w') as file:
            file.writelines(positive_lines)
            file.writelines(negative_lines)

    print(f"Les fichiers triés ont été enregistrés dans le dossier '{output_dir}'.")

# Exemple d'utilisation
input_dir = 'analyse_hot_file'           # Dossier d'entrée contenant les fichiers à traiter
output_dir = input_dir + "_sorted"       # Dossier de sortie pour enregistrer les fichiers triés
sort_and_group_lines_in_directory(input_dir, output_dir)

