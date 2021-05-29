pkill -f graphdb
pkill -f fuseki
#pkill -f blazegraph

rm virtuoso-opensource/database/virtuoso.db
rm virtuoso-opensource/database/virtuoso.trx
rm virtuoso-opensource/database/virtuoso-temp.db
rm virtuoso-opensource/database/virtuoso.pxa
rm virtuoso-opensource/database/virtuoso.lck
rm virtuoso-opensource/database/virtuoso.log
cd virtuoso-opensource/bin/ && ./virtuoso-t +configfile ../database/virtuoso.ini
echo $@
sleep 5m
#~/lemming/IGUANA/virtuoso-opensource/bin/isql 1111 dba dba exec="sparql WITH <http://example.com> DELETE {?s ?p ?o} WHERE {?s ?p ?o};checkpoint;"
~/lemming/IGUANA/virtuoso-opensource/bin/isql 1111 dba dba exec="ld_dir('$1','$2','http://example.com');"
~/lemming/IGUANA/virtuoso-opensource/bin/isql 1111 dba dba exec="rdf_loader_run();checkpoint;"
cd ~/lemming/IGUANA
