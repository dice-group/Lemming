pkill -f graphdb
pkill -f virtuoso
pkill -f fuseki
pkill -f tentris

./graphdb-10.7.3/bin/importrdf load --force -p -c graphdb/config.ttl -m parallel $1
# ./graphdb/bin/loadrdf --force -p -c graphdb/config.ttl -m parallel $1
cd graphdb-10.7.3/ && ./bin/graphdb -s -d

