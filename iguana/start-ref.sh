pkill -f blazegraph
rm ref/blazegraph.jnl
cd ref && /usr/lib/jvm/java-8-oracle/bin/java -server -Djetty.port=6666 -jar blazegraph.jar  &
sleep 10
curl -X POST -H 'Content-Type:text/turtle' --data-binary "@$1" "http://localhost:6666/blazegraph/sparql"


