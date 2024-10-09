import os
import re
import shutil
import tempfile
import argparse
from git import Repo

def find_java_files(directory):
    java_files = []
    for root, dirs, files in os.walk(directory):
        for file in files:
            if file.endswith(".java"):
                java_files.append(os.path.join(root, file))
    return java_files

def analyze_file(file_path, clazz):
    with open(file_path, 'r', encoding='utf-8') as file:
        content = file.read()

    matches = re.findall(rf'[a-zA-Z_][a-zA-Z0-9_]*\s*=\s*new\s+{clazz}', content)

    if matches:
        var_names = [match.split('=')[0].strip() for match in matches]

        for var in var_names:
            is_private = re.search(rf'private\s+[^\n]*\b{var}\b', content)
            is_private_str = "Y" if is_private else "N"

            methods = re.findall(rf'{var}\.(\w+)\(', content)
            methods = sorted(list(methods))

            results = []
            for method in methods:
                method_usage = re.search(rf'return\s+{var}\.{method}\(|=\s*{var}\.{method}\(', content)
                method_str = f"+{method}" if method_usage else method
                results.append(method_str)

            write_results_to_file(clazz, is_private_str, results)

def write_results_to_file(clazz, is_private_str, methods):
    filename = f"{clazz}.txt"
    with open(filename, 'a') as f:
        if is_private_str == "Y":
            for method in methods:
                f.write(f"{method} ")

def extract_imports(file_path):
    imports = []
    with open(file_path, 'r', encoding='utf-8') as file:
        for line in file:
            if line.strip().startswith("import"):
                imports.append(line.strip())

    file_name = os.path.basename(file_path).replace('.java', '')
    with open('liste_import_'+file_name+'.txt', 'a') as f:
        for imp in imports:
            f.write(f"{imp}\n")

def main(repo_url, list_clazz):
    TMPDIR = tempfile.mkdtemp()

    software = repo_url.split('/')[-1]
    repo_path = os.path.join(TMPDIR, software)
    print(f"Cloning {repo_url} into {repo_path}...")
    Repo.clone_from(repo_url, repo_path)

    java_files = find_java_files(repo_path)

    for java_file in java_files:
        extract_imports(java_file)
        for clazz in list_clazz:
            analyze_file(java_file, clazz)

    shutil.rmtree(repo_path)
    shutil.rmtree(TMPDIR)

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Analyse Java files for class instantiations and methods.")
    parser.add_argument("-r", dest="repo_url", help="The URL of the git repository to clone.")
    parser.add_argument("-c", dest="clazz", action="append", type=str, help="The Java class to search for (e.g., AtomicReference).")

    args = parser.parse_args()

    main(args.repo_url, args.clazz)