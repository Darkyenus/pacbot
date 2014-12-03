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

JAVAFILES=""

for JAVAFILE in $(find ../../shared/src/main/java ../../nxt/src/main/java -name *.java)
do
    JAVAFILES="$JAVAFILES $JAVAFILE"
done

echo "Compiling..."
../../lejos/bin/nxjc -d . -source 6 -target 6 ${JAVAFILES}
echo "Compiled"