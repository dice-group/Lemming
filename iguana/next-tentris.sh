graphFolder=$1
for f in "$graphFolder"/*
do
  if [ -f "$f" ]; then
    rm -rf queryCache/
    echo "executing $f"
    pkill -f tentris
    ./start-ref.sh $f
    ./start-tentris.sh $f
    sleep 1m
    gn="${f%%.*}"
    echo "$gn"
    cp example-suite.yml example-suiteTMP.yml
    queries=$( ./get_queries.sh $f )
    echo "Queries file: $queries"
    sed -i -e 's,QUERIES,'"$queries"',g' example-suiteTMP.yml
    sed -i -e 's,GraphName,'"$gn"',g' example-suiteTMP.yml
    sed -i -e 's,TSTORE,Tentris,g' example-suiteTMP.yml
    sed -i -e 's,ENDPOINT,http://localhost:9080/sparql,g' example-suiteTMP.yml
    ./start-iguana.sh example-suiteTMP.yml

    pkill -f tentris
  fi
  echo "done"
done

