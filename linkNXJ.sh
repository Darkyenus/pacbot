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

# At this point, compileNXJ.sh must have been called, so the classes are right here.

if [ -n "$2" ]
then
    PROGRAM_NAME="$2"
else
    PROGRAM_NAME="NXTProgram"
fi

MAIN_CLASS=$1

if [ -n "$MAIN_CLASS" ]
then
    echo "Linking $PROGRAM_NAME..."
    ../../lejos/bin/nxjlink -v -od linkDump -o ${PROGRAM_NAME}.nxj ${MAIN_CLASS} > debugInfo.txt
    echo "Linked, output saved in $COMPILEDIR/debugInfo.txt"
else
    echo "Usage: script <Main class>"
fi

