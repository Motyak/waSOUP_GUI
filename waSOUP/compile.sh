#!/bin/bash

# Create directories
mkdir -p bin lib collection/tracks

# Create database
$(cd collection && [ ! -f tracks.db ] && sqlite3 tracks.db "$(< createDatabase.sql)")

# Download dependencies
NB_OF_DEPS=$(jq length dependencies.json)
for i in $(seq 0 $((NB_OF_DEPS - 1))); do
    NAME=$(jq -r ".[$i].name" dependencies.json)
    VER=$(jq -r ".[$i].ver" dependencies.json)
    LINK=$(jq -r ".[$i].link" dependencies.json)
    FILENAME="lib/$NAME-$VER.jar"

    [ ! -f $FILENAME ] &&
        wget $LINK -O $FILENAME
done

# Generate slice2java interfaces
$(cd src && slice2java generatedIce.ice)

# Compile slice2java interfaces
ICE_VER=$(jq -c '.[] | select(.name == "ice")' dependencies.json | jq -r '.ver')
javac src/generatedIce/*.java -cp lib/ice-$ICE_VER.jar -d bin

# Compile soup classes
LIBS=$(for f in lib/*; do printf "$f:"; done)
javac src/soup/*.java -cp $LIBS:bin -d bin
