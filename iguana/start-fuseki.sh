#!/usr/bin/bash 

rm -rf /local/Lemming/iguana/fuseki/apache-jena-fuseki-5.1.0/DS/

/local/Lemming/iguana/fuseki/apache-jena-5.1.0/bin/tdb1.xloader --loc /local/Lemming/iguana/fuseki/apache-jena-fuseki-5.1.0/DS $1 
cd /local/Lemming/iguana/fuseki/apache-jena-fuseki-5.1.0/ && ./fuseki-server --loc=DS /ds &

sleep 1m
./wait_until_up.sh "http://localhost:3030/ds/sparql"

# Start iguana
./prep_iguana.sh $1 "Fuseki" "http://localhost:3030/ds/sparql"

pkill -f fuseki
