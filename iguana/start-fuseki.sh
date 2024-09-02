pkill -f virtuoso
pkill -f fuseki
pkill -f graphdb

rm -rf /local/Lemming/iguana/fuseki/apache-jena-fuseki-3.8.0/DS/

/local/Lemming/iguana/fuseki/apache-jena-3.8.0/bin/tdbloader2 --loc /local/Lemming/iguana/fuseki/apache-jena-fuseki-3.8.0/DS $1 
cd /local/Lemming/iguana/fuseki/apache-jena-fuseki-3.8.0/ && ./fuseki-server --loc=DS /ds &
sleep 1m

