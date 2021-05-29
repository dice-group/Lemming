graphFolder=$1
for f in $(ls $graphFolder)
do
	fullFile=$graphFolder$f
	rm -rf queryInstances/
	./start-ref.sh $fullFile
	echo "executing $fullFile"
	pkill -f fuseki
	./start-fuseki.sh $fullFile
	sleep 5m
	#gn=$(echo ${f:43} | sed -e 's/[^a-zA-Z0-9]//g')
	#gn="geology_target"
	gn="${f%%.*}"
	echo "$gn"
	cp iguana.config iguanaTMP.config
	sed -i -e 's,GraphName,'"$gn"',g' iguanaTMP.config
	sed -i -e 's,TSTORE,Fuseki,g' iguanaTMP.config
	sed -i -e 's,ENDPOINT,http://localhost:3030/ds/sparql,g' iguanaTMP.config
	./start-iguana.sh iguanaTMP.config
	pkill -f fuseki
	echo "done"
done
