pkill -f graphdb
pkill -f virtuoso
pkill -f fuseki

./graphdb/bin/loadrdf --force -p -c graphdb/config.ttl -m parallel $1
cd graphdb/ && ./bin/graphdb -s -d
