graphFolder=$1
for f in "$graphFolder"/*
do
  if [ -f "$f" ]; then
    rm -rf queryCache/
    echo "executing $f"
    pkill -f graphdb
    ./start-ref.sh $f
    ./start-graphdb.sh $f
    sleep 1m
    ./wait_until_up.sh "http://localhost:7200/repositories/repo"
    gn="${f%%.*}"
    echo "$gn"
    cp example-suite.yml example-suiteTMP.yml
    queries=$( ./get_queries.sh $f )
    echo "Queries file: $queries"
    sed -i -e 's,QUERIES,'"$queries"',g' example-suiteTMP.yml
    sed -i -e 's,GraphName,'"$gn"',g' example-suiteTMP.yml
    sed -i -e 's,TSTORE,GraphDB,g' example-suiteTMP.yml
    sed -i -e 's,ENDPOINT,http://localhost:7200/repositories/repo,g' example-suiteTMP.yml
    ./start-iguana.sh example-suiteTMP.yml

    pkill -f graphdb
  fi
  echo "done"
done

