#!/usr/bin/bash 

rm blazegraph.jnl
/local/Lemming/iguana/jdk1.8.0_202/bin/java -server -jar blazegraph.jar &
sleep 10
curl -X POST -H 'Content-Type:text/turtle' --data-binary "@$1" "http://localhost:9999/blazegraph/sparql"

sleep 1m
./wait_until_up.sh "http://localhost:9999/blazegraph/sparql"

# Start iguana
./prep_iguana.sh $1 "Blazegraph" "http://localhost:9999/blazegraph/sparql"

pkill -f blazegraph
