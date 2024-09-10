f=$1
queries="queries.txt"
if [[ "$f" == *SemanticWeb* ]]; then
  queries="swdf_queries.txt"
fi
if [[ "$f" == *LinkedGeo* ]]; then
  queries="lgeo_queries.txt"
fi
if [[ "$f" == *Geology* ]]; then
  queries="icc_queries.txt"
fi

echo "$queries"
