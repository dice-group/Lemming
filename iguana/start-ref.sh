#!/usr/bin/bash 

pkill -f fuseki

rm -rf /local/iguana/ref/apache-jena-fuseki-5.5.0/REF/

/local/iguana/ref/apache-jena-5.5.0/bin/tdb1.xloader --loc /local/iguana/ref/apache-jena-fuseki-5.5.0/REF $1 
cd /local/iguana/ref/apache-jena-fuseki-5.5.0/ && ./fuseki-server --port 3131 --loc=REF /ref &

sleep 1m

