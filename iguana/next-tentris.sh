graphFolder=$1
for f in $(ls $graphFolder)
do
	fullFile=$graphFolder$f
	rm -rf queryInstances/
	./start-ref.sh $fullFile
	echo "executing $f"
	pkill -f tentris
	./start-tentris.sh $graphFolder $f
	sleep 5m
	gn="${f%%.*}"
	echo "$gn"
	cp iguana.config iguanaTMP.config
	sed -i -e 's,GraphName,'"$gn"',g' iguanaTMP.config
	sed -i -e 's,TSTORE,Tentris,g' iguanaTMP.config
	sed -i -e 's,ENDPOINT,http://localhost:9080/sparql,g' iguanaTMP.config
	./start-iguana.sh iguanaTMP.config
	pkill -f tentris
	echo "done"
done