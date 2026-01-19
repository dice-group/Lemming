#!/usr/bin/bash 

filepath="/local/iguana/tentris-storage"
rm -rf $filepath

tentris -s $filepath load --file "$1" 
tentris -s $filepath serve 127.0.0.1:9080 &

# Wait until it's up
sleep 1m
./wait_until_up.sh "http://localhost:9080/sparql"
	
# Start iguana
./prep_iguana.sh "$1" "Tentris" "http://localhost:9080/sparql"

pkill -f tentris
