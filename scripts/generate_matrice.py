import os
import numpy as np

def process_files_in_directory(input_dir):
    files = os.listdir(input_dir)  # Récupérer tous les fichiers
    project_count = len(files)
    max_lines = 0

    # Liste temporaire pour stocker le nombre de signes '+' pour chaque fichier
    file_plus_counts = []

    # D'abord, calculer le nombre maximum de lignes dans un fichier et le nombre de signes '+' pour chaque fichier
    for filename in files:
        file_path = os.path.join(input_dir, filename)
        with open(file_path, 'r') as file:
            line_count = sum(1 for _ in file)  # Compte le nombre total de lignes
            max_lines = max(max_lines, line_count)

            # Compter le nombre de signes '+'
            plus_count = sum(1 for line in file if line.strip().split()[2] == '+')  # Compter les '+' dans le fichier
            file_plus_counts.append((filename, plus_count))  # Ajouter à la liste temporaire

    # Trier les fichiers en fonction du nombre de signes '+' (décroissant)
    file_plus_counts.sort(key=lambda x: x[1], reverse=True)

    # Initialiser les matrices avec `nan`
    matrix_plus = np.full((project_count, max_lines), np.nan)
    matrix_minus = np.full((project_count, max_lines), np.nan)

    # Parcourir chaque fichier trié et remplir les matrices
    for x, (filename, _) in enumerate(file_plus_counts):
        file_path = os.path.join(input_dir, filename)
        with open(file_path, 'r') as file:
            for y, line in enumerate(file):
                parts = line.strip().split()
                if len(parts) == 3:
                    modification_count, java_file, sign = int(parts[0]), parts[1], parts[2]
                    if sign == '+':
                        matrix_plus[x, y] = modification_count
                        matrix_minus[x, y] = np.nan
                    elif sign == '-':
                        matrix_minus[x, y] = modification_count
                        matrix_plus[x, y] = np.nan

    return matrix_plus, matrix_minus

def format_matrix_for_tikz(matrix):
    formatted_output = "\\addplot [matrix plot, point meta=explicit] coordinates {\n"
    rows, cols = matrix.shape
    for y in range(cols):
        for x in range(rows):
            value = "nan" if np.isnan(matrix[x, y]) else int(matrix[x, y])
            formatted_output += f"({x},{y}) [{value}] "
        formatted_output += "\n\n"
    formatted_output += "};\n"
    return formatted_output

def sort_matrix_columns_by_nan(matrix):
    nan_counts = np.isnan(matrix).sum(axis=0)  # Compter les nan dans chaque colonne
    sorted_indices = np.argsort(nan_counts)    # Indices triés par nombre croissant de nan
    return matrix[:, sorted_indices]

# Exemple d'utilisation
input_dir = 'analyse_hot_file_sorted'  # Dossier d'entrée contenant les fichiers à analyser
matrix_plus, matrix_minus = process_files_in_directory(input_dir)

# Afficher les matrices formatées pour TikZ
print("Matrice pour les fichiers avec signe '+':")
print(format_matrix_for_tikz(matrix_plus))

print("Matrice pour les fichiers avec signe '-':")
print(format_matrix_for_tikz(matrix_minus))