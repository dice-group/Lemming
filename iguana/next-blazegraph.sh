graphFolder=$1
for file in "$graphFolder"/*
do
  if [ -f "$file" ]; then
    rm -rf queryCache/
    echo "executing $file"
    pkill -f blazegraph
    f=$(realpath "$file")
    ./start-ref.sh $f
    ./start-blazegraph.sh $f
    sleep 1m
    ./wait_until_up.sh "http://localhost:9999/blazegraph/sparql"
    gn="${f%%.*}"
    echo "$gn"
    cp example-suite.yml example-suiteTMP.yml
    queries=$( ./get_queries.sh $f )
    save=$(basename "$f")"-Blazegraph"
    echo "Queries file: $queries"
    sed -i -e 's,QUERYFILE,'"$queries"',g' example-suiteTMP.yml
    sed -i -e 's,GraphName,'"$gn"',g' example-suiteTMP.yml
    sed -i -e 's,TSTORE,Blazegraph,g' example-suiteTMP.yml
    sed -i -e 's,ENDPOINT,http://localhost:9999/blazegraph/sparql,g' example-suiteTMP.yml
    sed -i -e 's,SAVEPATH,'"$save"',g' example-suiteTMP.yml
    ./start-iguana.sh example-suiteTMP.yml

    pkill -f blazegraph
  fi
  echo "done"
done

