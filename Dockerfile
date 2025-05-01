# Base image
FROM ubuntu:22.04

ENV DEBIAN_FRONTEND=noninteractive
ENV TZ=Europe/Paris

# Install system dependencies + Python 3.11 + Maven + numactl
RUN apt-get update \
 && apt-get install -y \
    software-properties-common \
    wget curl unzip git gnupg ca-certificates lsb-release \
    python3.11 python3.11-dev python3.11-venv python3-pip \
    maven \
    numactl \
 && ln -sf /usr/bin/python3.11 /usr/bin/python \
 && ln -sf /usr/bin/python3.11 /usr/bin/python3

# Install required Python packages (gdown, gitpython, javalang, numpy)
RUN pip install --no-cache-dir \
    gdown \
    gitpython \
    javalang \
    numpy

# Manually install OpenJDK 22 from Adoptium
RUN mkdir -p /opt/java \
 && wget -q https://github.com/adoptium/temurin22-binaries/releases/download/jdk-22%2B36/OpenJDK22U-jdk_x64_linux_hotspot_22_36.tar.gz \
 && tar -xzf OpenJDK22U-jdk_x64_linux_hotspot_22_36.tar.gz -C /opt/java --strip-components=1 \
 && rm OpenJDK22U-jdk_x64_linux_hotspot_22_36.tar.gz

# Set JAVA_HOME and add Java 22 to the PATH
ENV JAVA_HOME=/opt/java
ENV PATH="$JAVA_HOME/bin:$PATH"

# Set working directory inside the container
WORKDIR /app

# Copy Java source code and all necessary scripts into the container
COPY java/ /app/java/
COPY microbenchmark_results/ /app/microbenchmark_results/
COPY experiences/download.sh run_experiences.sh test.sh run_mining.sh *.py ./

# Make scripts executable
RUN chmod +x download.sh run_experiences.sh test.sh run_mining.sh

# Default entrypoint: run download.sh
ENTRYPOINT ["./download.sh"]

# Default command passed to download.sh
CMD ["./run_experiences.sh"]
