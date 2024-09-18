#!/usr/bin/bash 

f=$1
tstore=$2
endpoint=$3

gn="${f%%.*}"
echo "$gn"
cp example-suite.yml example-suiteTMP.yml
queries=$( ./get_queries.sh $f )
echo "Queries file: $queries"
sed -i -e 's,QUERYFILE,'"$queries"',g' example-suiteTMP.yml
sed -i -e 's,GraphName,'"$gn"',g' example-suiteTMP.yml
sed -i -e 's,TSTORE,'"$tstore"',g' example-suiteTMP.yml
sed -i -e 's,ENDPOINT,'"$endpoint"',g' example-suiteTMP.yml
./start-iguana.sh example-suiteTMP.yml
