import os
import numpy as np

def process_files_in_directory(input_dir):
    files = os.listdir(input_dir)
    project_count = len(files)
    max_lines = 0

    file_plus_counts = []

    for filename in files:
        file_path = os.path.join(input_dir, filename)
        with open(file_path, 'r') as file:
            line_count = sum(1 for _ in file)
            max_lines = max(max_lines, line_count)

            file.seek(0)

            plus_count = sum(1 for line in file if line.strip().split()[2] == '+')
            file_plus_counts.append((filename, plus_count))

    file_plus_counts.sort(key=lambda x: x[1], reverse=True)

    # Initialize matrices
    matrix_plus = np.full((project_count, max_lines), np.nan)
    matrix_minus = np.full((project_count, max_lines), np.nan)

    for x, (filename, _) in enumerate(file_plus_counts):
        file_path = os.path.join(input_dir, filename)
        with open(file_path, 'r') as file:
            for y, line in enumerate(file):
                parts = line.strip().split()
                if len(parts) == 3:
                    modification_count, java_file, sign = int(parts[0]), parts[1], parts[2]
                    if sign == '+':
                        matrix_plus[x, y] = modification_count
                    elif sign == '-':
                        matrix_minus[x, y] = modification_count

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

def save_to_file(content, filename):
    with open(filename, 'w') as f:
        f.write(content)

def sort_matrix_columns_by_nan(matrix):
    nan_counts = np.isnan(matrix).sum(axis=0)
    sorted_indices = np.argsort(nan_counts)
    return matrix[:, sorted_indices]

# === MAIN EXECUTION ===

input_dir = 'analyse_hot_file_sorted'  # Folder containing input files

matrix_plus, matrix_minus = process_files_in_directory(input_dir)

# Format matrices
tikz_plus = format_matrix_for_tikz(matrix_plus)
tikz_minus = format_matrix_for_tikz(matrix_minus)

# Save to .tex files
save_to_file(tikz_plus, 'results/include_shared_object.tex')
save_to_file(tikz_minus, 'results/not_include_shared_object.tex')

print("Files 'include_shared_object.tex' and 'not_include_shared_object.tex' generated successfully.")
