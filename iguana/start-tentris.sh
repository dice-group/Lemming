pkill -f graphdb
pkill -f virtuoso
pkill -f blazegraph
pkill -f tentris

filepath="/local/Lemming/iguana/tentris-storage"
rm -rf $filepath

./tentris_loader --file $1 --storage $filepath
./tentris_server -p 9080 --storage $filepath &
	
