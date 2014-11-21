#!/bin/sh

### Shared header
cd "$(dirname "$0")"

COMPILEDIR="target/nxj"

mkdir -p ${COMPILEDIR}

cd ${COMPILEDIR}

if [[ $OSTYPE == darwin* ]]; then
    export JAVA_HOME="/System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home/"
fi

#######

mkdir loaderprograms
cd loaderprograms

echo "Creating and uploading eight Loaders..."
for I in {1..8}; do
    LOADER_FILE="MapLoader.java"
    cat ../../../nxt/src/main/java/MapLoader.java | sed "s/#/${I}/g" > $LOADER_FILE

    ../../../lejos/bin/nxjc $LOADER_FILE
    ../../../lejos/bin/nxj -o Map${I}.nxj -u MapLoader
done