#!/usr/bin/bash 
f=$1
queries="queries.txt"
if [[ "$f" == *SemanticWeb* ]]; then
  queries="swdf.benchmark.txt"
fi
if [[ "$f" == *LinkedGeo* ]]; then
  queries="lgeo.benchmark.txt"
fi
if [[ "$f" == *Geology* ]]; then
  queries="icc.benchmark.txt"
fi

echo "$queries"
