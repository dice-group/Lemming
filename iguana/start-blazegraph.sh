pkill -f virtuoso
pkill -f fuseki
pkill -f graphdb

rm blazegraph.jnl
/local/Lemming/iguana/jdk1.8.0_202/bin/java -server -jar blazegraph.jar &
sleep 10
curl -X POST -H 'Content-Type:text/turtle' --data-binary "@$1" "http://localhost:9999/blazegraph/sparql"

