#!/usr/bin/bash 

pkill -f graphdb
pkill -f virtuoso
pkill -f blazegraph

filepath="/local/Lemming/iguana/tentris-storage"
rm -rf $filepath

./tentris_loader --file $1 --storage $filepath
./tentris_server -p 9080 --storage $filepath &

# Wait until it's up
sleep 1m
./wait_until_up.sh "http://localhost:9080/sparql"
	
# Start iguana
./prep_iguana.sh $1 "Tentris" "http://localhost:9080/sparql"

pkill -f tentris
