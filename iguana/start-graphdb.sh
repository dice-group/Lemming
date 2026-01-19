#!/usr/bin/bash 

# Start GraphDB
./graphdb-11.1.0/bin/importrdf load --force -p -c graphdb-11.1.0/config.ttl -m parallel "$1"
./graphdb-11.1.0/bin/graphdb -s -d

# Wait until it's up
sleep 1m
./wait_until_up.sh "http://localhost:7200/repositories/repo"

# Start iguana
./prep_iguana.sh "$1" "GraphDB" "http://localhost:7200/repositories/repo"

pkill -f graphdb
