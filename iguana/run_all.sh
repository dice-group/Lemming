#!/usr/bin/bash 

mkdir -p results/
mkdir -p rdf-results/
mkdir -p queries/queryInstances/

# For each graph in the folder, run all triplestores
graphFolder=$1
for file in "$graphFolder"/*
do
  if [ -f "$file" ]; then
    f=$(realpath "$file")
    fileName=$(basename "$file")
    echo "executing $file"
    
    # Start reference endpoint
    gn="${f%%.*}"
    pkill -f fuseki
    ./start-ref.sh $f
    
    # Run all with the same query instances
    ./start-blazegraph.sh $f
    ./start-virtuoso.sh $f
    ./start-graphdb.sh $f
    ./start-tentris.sh $f
    ./start-fuseki.sh $f
    
    # Reset query instance cache for each graph
    # Rename it based on file name
    queryFile=${fileName%%.*}_queries.txt
    if [[ "$f" == *"initial"* ]]; then
      queryFile="initial_"$queryFile
    elif [[ "$f" == *"mimic"* ]]; then
      queryFile="mimic_"$queryFile
    else 
      echo "I don't know this folder, not adding prefix to $queryFile"
    fi
    
    mv queries/*instances* queries/queryInstances/$queryFile
  fi
  echo "done"
done

