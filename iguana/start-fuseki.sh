pkill -f virtuoso
pkill -f fuseki
pkill -f graphdb

rm -rf ~/lemming/IGUANA/fuseki/apache-jena-fuseki-3.8.0/DS/

~/lemming/IGUANA/fuseki/apache-jena-3.8.0/bin/tdbloader2 --loc ~/lemming/IGUANA/fuseki/apache-jena-fuseki-3.8.0/DS $1 
cd ~/lemming/IGUANA/fuseki/apache-jena-fuseki-3.8.0/ && ./fuseki-server --loc=DS /ds &
sleep 1m

