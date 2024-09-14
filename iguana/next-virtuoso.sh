graphFolder=$1
for file in "$graphFolder"/*
do
  if [ -f "$file" ]; then
    rm -rf queryCache/
    echo "executing $file"
    pkill -f virtuoso
    f=$(realpath "$file")
    file_name=$(basename "$f")
    dir_path=$(dirname "$f")
    ./start-ref.sh $f
    ./start-virtuoso.sh $dir_path $file_name
    sleep 1m
    ./wait_until_up.sh "http://localhost:8890/sparql"
    gn="${f%%.*}"
    echo "$gn"
    cp example-suite.yml example-suiteTMP.yml
    queries=$( ./get_queries.sh $f )
    echo "Queries file: $queries"
    sed -i -e 's,QUERIES,'"$queries"',g' example-suiteTMP.yml
    sed -i -e 's,GraphName,'"$gn"',g' example-suiteTMP.yml
    sed -i -e 's,TSTORE,Virtuoso,g' example-suiteTMP.yml
    sed -i -e 's,ENDPOINT,http://localhost:8890/sparql,g' example-suiteTMP.yml
    sed -i -e 's,SAVEPATH,'"${gn}-iguana"',g' example-suiteTMP.yml
    ./start-iguana.sh example-suiteTMP.yml

    pkill -f virtuoso
  fi
  echo "done"
done

