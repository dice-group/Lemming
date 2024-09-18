#!/usr/bin/bash 

pkill -f graphdb
pkill -f fuseki
pkill -f tentris
pkill -f virtuoso

f=$1
file_name=$(basename "$f")
dir_path=$(dirname "$f")

rm virtuoso-opensource/database/virtuoso.db
rm virtuoso-opensource/database/virtuoso.trx
rm virtuoso-opensource/database/virtuoso-temp.db
rm virtuoso-opensource/database/virtuoso.pxa
rm virtuoso-opensource/database/virtuoso.lck
rm virtuoso-opensource/database/virtuoso.log
cd virtuoso-opensource/bin/ && ./virtuoso-t +configfile ../database/virtuoso.ini
echo $@
sleep 1m
/local/Lemming/iguana/virtuoso-opensource/bin/isql 1111 dba dba exec="sparql WITH <http://example.com> DELETE {?s ?p ?o} WHERE {?s ?p ?o};checkpoint;"
/local/Lemming/iguana/virtuoso-opensource/bin/isql 1111 dba dba exec="ld_dir('$dir_path','$file_name','http://example.com');"
/local/Lemming/iguana/virtuoso-opensource/bin/isql 1111 dba dba exec="rdf_loader_run();checkpoint;"
cd /local/Lemming/iguana

sleep 1m
./wait_until_up.sh "http://localhost:8890/sparql"

./prep_iguana.sh $f "Virtuoso" "http://localhost:8890/sparql"
