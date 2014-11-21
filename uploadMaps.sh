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

echo "Uploading maps with params: -u ..."
../../lejos/bin/nxjupload -u ../../bot/maps
echo "Uploaded"