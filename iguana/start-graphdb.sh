#!/usr/bin/bash 

pkill -f graphdb
pkill -f virtuoso
pkill -f blazegraph
pkill -f tentris

# Start GraphDB
./graphdb-10.7.3/bin/importrdf load --force -p -c graphdb/config.ttl -m parallel $1
cd graphdb-10.7.3/ && ./bin/graphdb -s -d

# Wait until it's up
sleep 1m
./wait_until_up.sh "http://localhost:7200/repositories/repo"

# Start iguana
./prep_iguana.sh $1 "GraphDB" "http://localhost:7200/repositories/repo"

