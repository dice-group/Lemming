#!/bin/bash

ENDPOINT_URL=$1
SPARQL_QUERY='SELECT (COUNT(*) AS ?count) WHERE { ?s ?p ?o } LIMIT 1'
CHECK_INTERVAL=10

# Loop until the triplestore is confirmed to be up
while true; do
    HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" --data-urlencode "query=${SPARQL_QUERY}" "$ENDPOINT_URL")
    if [ "$HTTP_STATUS" -eq 200 ]; then
        echo "Triplestore is up and ready."
        exit 0
    else
        echo "Failed to connect to the triplestore. HTTP status code: $HTTP_STATUS"
        echo "Retrying in $CHECK_INTERVAL seconds..."
        sleep "$CHECK_INTERVAL"
    fi
done
