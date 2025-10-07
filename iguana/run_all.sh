#!/usr/bin/bash 

mkdir -p results/
mkdir -p queries/queryInstances/

# For each graph in the folder, run all triplestores
graphFolder=$1
for file in "$graphFolder"/*
do
  if [ -f "$file" ]; then
    f=$(realpath "$file")
    fileName=$(basename "$file")
    echo "executing $file"
    
    # Make sure all instances are finished
    pkill -f fuseki
    pkill -f tentris
    pkill -f graphdb
    pkill -f virtuoso
    pkill -f blazegraph
    #sleep 1m
    
    gn="${f%%.*}"
    
    # Run all with the same query instances
    ./start-blazegraph.sh $f
    ./start-virtuoso.sh $f
    ./start-graphdb.sh $f
    ./start-tentris.sh $f
    ./start-fuseki.sh $f
  fi
  echo "done"
done

