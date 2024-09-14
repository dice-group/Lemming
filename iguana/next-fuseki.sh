graphFolder=$1
for file in "$graphFolder"/*
do
  if [ -f "$file" ]; then
    rm -rf queryCache/
    echo "executing $file"
    pkill -f fuseki
    f=$(realpath "$file")
    ./start-ref.sh $f
    ./start-fuseki.sh $f
    sleep 1m
    ./wait_until_up.sh "http://localhost:3030/ds/sparql"
    gn="${f%%.*}"
    echo "$gn"
    cp example-suite.yml example-suiteTMP.yml
    queries=$( ./get_queries.sh $f )
    echo "Queries file: $queries"
    sed -i -e 's,QUERYFILE,'"$queries"',g' example-suiteTMP.yml
    sed -i -e 's,GraphName,'"$gn"',g' example-suiteTMP.yml
    sed -i -e 's,TSTORE,Fuseki,g' example-suiteTMP.yml
    sed -i -e 's,ENDPOINT,http://localhost:3030/ds/sparql,g' example-suiteTMP.yml
    sed -i -e 's,SAVEPATH,'"${gn}-iguana"',g' example-suiteTMP.yml
    ./start-iguana.sh example-suiteTMP.yml

    pkill -f fuseki
  fi
  echo "done"
done

