pkill -f fuseki

rm -rf /local/Lemming/iguana/fuseki/apache-jena-fuseki-5.1.0/REF/

/local/Lemming/iguana/fuseki/apache-jena-5.1.0/bin/tdb1.xloader --loc /local/Lemming/iguana/fuseki/apache-jena-fuseki-5.1.0/REF $1 
cd /local/Lemming/iguana/fuseki/apache-jena-fuseki-5.1.0/ && ./fuseki-server --port 3131 --loc=REF /ref &

