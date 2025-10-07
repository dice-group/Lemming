#!/usr/bin/bash 

# Start GraphDB
./graphdb/bin/importrdf load --force -p -c graphdb/config.ttl -m parallel $1
./graphdb/bin/graphdb -s -d

# Wait until it's up
sleep 1m
./wait_until_up.sh "http://localhost:7200/repositories/repo"

# Start iguana
./prep_iguana.sh $1 "GraphDB" "http://localhost:7200/repositories/repo"

pkill -f graphdb
