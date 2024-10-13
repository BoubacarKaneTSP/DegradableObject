import datetime
import os
import re
import shutil
import tempfile
import argparse
from git import Repo

# Fonction pour trouver les fichiers Java
def find_java_files(directory):
    java_files = []
    for root, dirs, files in os.walk(directory):
        for file in files:
            if file.endswith(".java"):
                java_files.append(os.path.join(root, file))
    return java_files

# Fonction pour extraire la variable et son utilisation
def analyze_file(file_path, clazz):
    with open(file_path, 'r', encoding='utf-8') as file:
        content = file.read()

    # Trouver les instances de la classe
    matches = re.findall(rf'[a-zA-Z_][a-zA-Z0-9_]*\s*=\s*new\s+{clazz}', content)

    if matches:
        var_names = [match.split('=')[0].strip() for match in matches]

        for var in var_names:
            # Vérifier si la variable est privée
            is_private = re.search(rf'private\s+[^\n]*\b{var}\b', content)
            is_private_str = "Y" if is_private else "N"

            # Extraire les méthodes appelées sur cette variable
            class_name = os.path.basename(file_path).replace('.java', '')
            methods = re.findall(rf'{var}\.(\w+)\(', content)
            methods = sorted(list(methods))

            # Vérifier si les méthodes sont utilisées dans un return ou une affectation
            results = []
            for method in methods:
                method_usage = re.search(rf'return\s+{var}\.{method}\(|=\s*{var}\.{method}\(', content)
                method_str = f"+{method}" if method_usage else method
                results.append(method_str)

            # Écriture des résultats dans un fichier texte
            write_results_to_file(clazz, class_name, is_private_str, results)

    return len(matches)

# Fonction pour écrire les méthodes dans un fichier
def write_results_to_file(clazz, class_name, is_private_str, methods):
    filename = f"{clazz}.txt"  # Le fichier porte le nom de la classe analysée
    with open(filename, 'a') as f:  # Mode 'a' pour ajouter à la suite du fichier
        if is_private_str == "Y":
            for method in methods:
                f.write(f"{method} ")

def extract_imports(file_path):
    imports = []
    with open(file_path, 'r', encoding='utf-8') as file:
        for line in file:
            # Vérifier si la ligne commence par "import"
            if line.strip().startswith("import"):
                imports.append(line.strip())

    # Écriture des imports dans un fichier liste_import.txt
    with open('liste_import.txt', 'a') as f:
        for imp in imports:
            f.write(f"{imp}\n")

def analyze_calls_in_methods(file_path, clazz):
    with open(file_path, 'r', encoding='utf-8') as file:
        content = file.read()

    # Trouver les instances de la classe
    matches = re.findall(rf'[a-zA-Z_][a-zA-Z0-9_]*\s*=\s*new\s+{clazz}', content)

    dico_vars = dict()

    if matches:
        var_names = [match.split('=')[0].strip() for match in matches]

        for var in var_names:
            # Vérifier si la variable est privée
            is_private = re.search(rf'private\s+[^\n]*\b{var}\b', content)

            if is_private:
                dico_vars[var] = list()

    with open(file_path, 'r', encoding='utf-8') as file:
        lines = file.readlines()

    # Regular expression to find method call patterns
    method_call_pattern = re.compile(r'(\w+)\.(\w+)\(([^)]*)\)')

    current_method = "None"
    brace_count = 0
    in_block_comment = False


    for i, line in enumerate(lines):
        # Check for block comments
        if '/*' in line:
            in_block_comment = True
        if '*/' in line:
            in_block_comment = False
            continue  # Skip this line as it ends a block comment

        # Skip line if in a block comment or starts with //
        if in_block_comment or line.strip().startswith('//'):
            continue

        # Detect class definitions
        # class_match = re.search(r'\bclass\s+([A-Z]\w*)', line)
        # if class_match:
        #     current_method = "None"
        #     brace_count = 0

        # Detect method definitions
        method_match = re.search(r'\b(public|protected|private)?\s*(static\s+)?(final\s+)?([\w<>[\]?]+)\s+(\w+)\s*\(([^)]*)\)\s*{', line)
        if method_match:
            current_method = method_match.group(5)
            brace_count = 1

        # Adjust brace count and detect end of methods
        brace_count += line.count('{') - line.count('}')

        if brace_count == 0:
            current_method = "None"

        for match in method_call_pattern.finditer(line):
            obj, method, arguments = match.groups()

            if dico_vars.keys().__contains__(obj):
                if dico_vars[obj].keys().__contains__(current_method):
                    dico_vars[obj][current_method].append(method)
                else:
                    dico_vars[obj][current_method] = list()

    for obj in dico_vars.keys():
        for m_c in dico_vars[obj].keys():
            print(obj + " used in " + m_c + " with method ", dico_vars[obj][m_c])

def get_repo_age(repo):
    # Obtenir le premier commit (le plus ancien)
    oldest_commit = next(repo.iter_commits(reverse=True))
    # Récupérer l'année du plus ancien commit
    oldest_commit_year = datetime.datetime.fromtimestamp(oldest_commit.committed_date).year
    return oldest_commit_year

# Fonction principale
def main(repo_url, list_clazz, evolution):
    TMPDIR = tempfile.mkdtemp()

    # Cloner le dépôt dans un répertoire temporaire
    software = repo_url.split('/')[-1]
    repo_path = os.path.join(TMPDIR, software)
    print(f"Cloning {repo_url} into {repo_path}...")
    repo = Repo.clone_from(repo_url, repo_path)

    if evolution:
        oldest_commit_year = get_repo_age(repo)
        current_year = datetime.datetime.now().year

        if current_year - oldest_commit_year < 10:
            print(f"Repo {repo_url} has less than 10 years of history. Skipping.")
        else:

            for i in range(10):

                year = current_year - i
                target_commit = None
                nb_match = dict()
                for clazz in list_clazz:
                    nb_match[clazz] = 0

                for commit in repo.iter_commits():
                    commit_year = datetime.datetime.fromtimestamp(commit.committed_date).year
                    if commit_year == year:
                        target_commit = commit
                        break

                if target_commit:
                    # Se placer sur le commit de l'année spécifiée
                    repo.git.checkout(target_commit.hexsha)
                    print(f"Checked out commit {target_commit.hexsha} from {year}")
                    java_files = find_java_files(repo_path)
                    for java_file in java_files:

                        for clazz in list_clazz:
                            nb_match[clazz] += analyze_file(java_file, clazz)

                    for clazz in list_clazz:
                        filename = f"yearly_evolution_{clazz}.txt"

                        with open(filename, 'a') as f:
                            f.write(str(year) + " " + str(nb_match[clazz]) + "\n")
                else:
                    print(f"No commit found for the year {year}")
    else:
        # Trouver tous les fichiers Java dans le dépôt
        java_files = find_java_files(repo_path)

        # Analyser chaque fichier Java pour la classe donnée
        for java_file in java_files:
            # extract_imports(java_file)
            for clazz in list_clazz:
                analyze_calls_in_methods(java_file, clazz)
                analyze_file(java_file, clazz)

    # Supprimer le dépôt cloné après analyse
    shutil.rmtree(repo_path)
    # Nettoyage du répertoire temporaire
    shutil.rmtree(TMPDIR)

# Lecture des arguments de la ligne de commande
if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Analyse Java files for class instantiations and methods.")
    parser.add_argument("-r", dest="repo_url", help="The URL of the git repository to clone.")
    parser.add_argument("-c", dest="clazz", action="append", type=str, help="The Java class to search for (e.g., AtomicReference).")
    parser.add_argument("-e", dest="evolution", action='store_true', help="Check the nb of declaration of clazz in the last 10 years")

    args = parser.parse_args()

    # Lancer l'analyse avec les paramètres fournis
    main(args.repo_url, args.clazz, args.evolution)