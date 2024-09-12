graphFolder=$1
for f in "$graphFolder"/*
do
  if [ -f "$f" ]; then
    rm -rf queryCache/
    echo "executing $f"
    pkill -f blazegraph
    ./start-ref.sh $f
    ./start-blazegraph.sh $f
    sleep 1m
    ./wait_until_up.sh "http://localhost:9999/blazegraph/sparql"
    gn="${f%%.*}"
    echo "$gn"
    cp example-suite.yml example-suiteTMP.yml
    queries=$( ./get_queries.sh $f )
    echo "Queries file: $queries"
    sed -i -e 's,QUERIES,'"$queries"',g' example-suiteTMP.yml
    sed -i -e 's,GraphName,'"$gn"',g' example-suiteTMP.yml
    sed -i -e 's,TSTORE,Blazegraph,g' example-suiteTMP.yml
    sed -i -e 's,ENDPOINT,http://localhost:9999/blazegraph/sparql,g' example-suiteTMP.yml
    ./start-iguana.sh example-suiteTMP.yml

    pkill -f blazegraph
  fi
  echo "done"
done

