graphFolder=$1
for f in "$graphFolder"/*
do
  if [ -f "$f" ]; then
    rm -rf queryCache/
    echo "executing $f"
    pkill -f virtuoso
    ./start-ref.sh $f
    ./start-virtuoso.sh $f
    sleep 5m
    gn="${f%%.*}"
    echo "$gn"
    cp example-suite.yml example-suiteTMP.yml
    queries=$( ./get_queries.sh $f )
    echo "Queries file: $queries"
    sed -i -e 's,QUERIES,'"$queries"',g' example-suiteTMP.yml
    sed -i -e 's,GraphName,'"$gn"',g' example-suiteTMP.yml
    sed -i -e 's,TSTORE,Virtuoso,g' example-suiteTMP.yml
    sed -i -e 's,ENDPOINT,http://localhost:8890/sparql,g' example-suiteTMP.yml
    ./start-iguana.sh example-suiteTMP.yml

    pkill -f virtuoso
  fi
  echo "done"
done

