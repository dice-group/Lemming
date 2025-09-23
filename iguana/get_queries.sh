#!/usr/bin/bash 
f=$1
queries="queries.txt"
if [[ "$f" == *SemanticWeb* ]]; then
  queries="swdf.benchmark-no.txt"
fi
if [[ "$f" == *LinkedGeo* ]]; then
  queries="lgeo.benchmark-no.txt"
fi
if [[ "$f" == *Geology* ]]; then
  queries="icc.benchmark-no.txt"
fi
if [[ "$f" == *DBPedia* ]]; then
  queries="dbpedia22.benchmark.txt"
fi
if [[ "$f" == *YAGO310* ]]; then
  queries="yago310.benchmark.txt"
fi

echo "$queries"
