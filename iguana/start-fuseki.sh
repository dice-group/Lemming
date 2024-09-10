pkill -f virtuoso
pkill -f fuseki
pkill -f graphdb
pkill -f tentris

# rm -rf /local/Lemming/iguana/fuseki/apache-jena-fuseki-3.8.0/DS/
rm -rf /local/Lemming/iguana/fuseki/apache-jena-fuseki-5.1.0/DS/

/local/Lemming/iguana/fuseki/apache-jena-5.1.0/bin/tdb1.xloader --loc /local/Lemming/iguana/fuseki/apache-jena-fuseki-5.1.0/DS $1 
cd /local/Lemming/iguana/fuseki/apache-jena-fuseki-5.1.0/ && ./fuseki-server --loc=DS /ds &
sleep 1m
