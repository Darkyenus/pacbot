#!/bin/sh

### Shared header
cd "$(dirname "$0")"/..

COMPILEDIR="target/nxj"

mkdir -p ${COMPILEDIR}

cd ${COMPILEDIR}

if [[ $OSTYPE == darwin* ]]; then
    export JAVA_HOME="/System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home/"
fi

#######

mkdir loaderprograms
cd loaderprograms

SCRIPT_SUFFIX=""

if [[ $OSTYPE == cygwin* ]]; then
    SCRIPT_SUFFIX=".bat"
fi

echo "Creating and uploading eight Loaders..."
for I in {1..8}; do
    LOADER_FILE="MapLoader.java"
    cat ../../../bot/MapLoader.java | sed "s/#/${I}/g" > $LOADER_FILE

    ../../../lejos/bin/nxjc$SCRIPT_SUFFIX $LOADER_FILE
    ../../../lejos/bin/nxj$SCRIPT_SUFFIX -o Map${I}.nxj -u MapLoader
done