graphFolder=$1
for file in "$graphFolder"/*
do
  if [ -f "$file" ]; then
    rm -rf queryCache/
    echo "executing $file"
    pkill -f tentris
    f=$(realpath "$file")
    ./start-ref.sh $f
    ./start-tentris.sh $f
    sleep 1m
    ./wait_until_up.sh "http://localhost:9080/sparql"
    gn="${f%%.*}"
    echo "$gn"
    cp example-suite.yml example-suiteTMP.yml
    queries=$( ./get_queries.sh $f )
    echo "Queries file: $queries"
    sed -i -e 's,QUERYFILE,'"$queries"',g' example-suiteTMP.yml
    sed -i -e 's,GraphName,'"$gn"',g' example-suiteTMP.yml
    sed -i -e 's,TSTORE,Tentris,g' example-suiteTMP.yml
    sed -i -e 's,ENDPOINT,http://localhost:9080/sparql,g' example-suiteTMP.yml
    sed -i -e 's,SAVEPATH,'"${gn}-iguana"',g' example-suiteTMP.yml
    ./start-iguana.sh example-suiteTMP.yml

    pkill -f tentris
  fi
  echo "done"
done

