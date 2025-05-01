# Image de base
FROM ubuntu:22.04

ENV DEBIAN_FRONTEND=noninteractive
ENV TZ=Europe/Paris

# Installer dépendances système + Python 3.11 + Maven + numactl
RUN apt-get update \
 && apt-get install -y \
    software-properties-common \
    wget curl unzip git gnupg ca-certificates lsb-release \
    python3.11 python3.11-dev python3.11-venv python3-pip \
    maven \
    numactl \
 && ln -sf /usr/bin/python3.11 /usr/bin/python \
 && ln -sf /usr/bin/python3.11 /usr/bin/python3

# Installer les dépendances Python nécessaires (gdown, gitpython, javalang, numpy)
RUN pip install --no-cache-dir \
    gdown \
    gitpython \
    javalang \
    numpy

# Installer manuellement OpenJDK 22 depuis Adoptium
RUN mkdir -p /opt/java \
 && wget -q https://github.com/adoptium/temurin22-binaries/releases/download/jdk-22%2B36/OpenJDK22U-jdk_x64_linux_hotspot_22_36.tar.gz \
 && tar -xzf OpenJDK22U-jdk_x64_linux_hotspot_22_36.tar.gz -C /opt/java --strip-components=1 \
 && rm OpenJDK22U-jdk_x64_linux_hotspot_22_36.tar.gz

# Définir JAVA_HOME et mettre Java 22 dans le PATH
ENV JAVA_HOME=/opt/java
ENV PATH="$JAVA_HOME/bin:$PATH"

# Définir le répertoire de travail
WORKDIR /app

# Copier le code Java et les scripts dans le conteneur
COPY java/ /app/java/
COPY analyse_hot_file/ /app/analyse_hot_file
COPY analyse_hot_file_sorted/ /app/analyse_hot_file_sorted
COPY microbenchmark_results/ /app/microbenchmark_results/
COPY experiences/download.sh run_experiences.sh test.sh run_mining.sh *.py ./

# Rendre les scripts exécutables
RUN chmod +x download.sh run_experiences.sh test.sh run_mining.sh

# Exécution du script de téléchargement, puis des expériences
ENTRYPOINT ["./download.sh"]
CMD ["./run_experiences.sh"]
