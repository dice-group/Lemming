pkill -f graphdb
pkill -f virtuoso
pkill -f fuseki

filepath="~/lemming/IGUANA/tentris-storage"
rm -rf $filepath

./tentris_loader --file $1 --storage $filepath
./tentris_server -p 9080 --storage $filepath

sleep 1m	