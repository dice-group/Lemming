#!/usr/bin/bash 

# For each graph in the folder, run all triplestores
graphFolder=$1
for file in "$graphFolder"/*
do
  if [ -f "$file" ]; then
    f=$(realpath "$file")
    echo "executing $file"
    
    # Start reference endpoint
    gn="${f%%.*}"
    pkill -f blazegraph
    ./start-ref.sh $f
    
    # Run all with the same query instances
    ./start-blazegraph.sh $f
    ./start-fuseki.sh $f
    ./start-virtuoso.sh $f
    ./start-graphdb.sh $f
    ./start-tentris.sh $f
    
    # Reset query instance cache for each graph
    # Rename it based on file name
    mv *instances* ${gn}_queries.txt
  fi
  echo "done"
done

