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

if [[ $1 == -* || -z $1 ]]; then
    #First parameter is arg or there are no parameters
    PROGRAM_NAME="NXTProgram"
    UPLOAD_PARAMS="${@:1}"
else
    PROGRAM_NAME="$1"
    UPLOAD_PARAMS="${@:2}"
fi

echo "Uploading $PROGRAM_NAME with params: $UPLOAD_PARAMS..."
../../lejos/bin/nxjupload ${UPLOAD_PARAMS} ${PROGRAM_NAME}.nxj
echo "Uploaded"