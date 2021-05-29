pkill -f virtuoso
pkill -f fuseki
pkill -f graphdb

rm blazegraph.jnl
/usr/lib/jvm/java-8-oracle/bin/java -server -jar blazegraph.jar &
sleep 10
curl -X POST -H 'Content-Type:text/turtle' --data-binary "@$1" "http://localhost:9999/blazegraph/sparql"

