graphFolder=$1
for f in $(ls $graphFolder)
do
	fullFile=$graphFolder$f
	rm -rf queryInstances/
	./start-ref.sh $fullFile
	echo "executing $f"
	pkill -f virtuoso
	./start-virtuoso.sh $graphFolder $f
	sleep 5m
	#gn=$(echo ${$f:43} | sed -e 's/[^a-zA-Z0-9]//g')
	#gn="geology_target"
	gn="${f%%.*}"
	echo "$gn"
	cp iguana.config iguanaTMP.config
	sed -i -e 's,GraphName,'"$gn"',g' iguanaTMP.config
	sed -i -e 's,TSTORE,Virtoso,g' iguanaTMP.config
	sed -i -e 's,ENDPOINT,http://localhost:8890/sparql,g' iguanaTMP.config
	./start-iguana.sh iguanaTMP.config
	pkill -f virtuoso
	echo "done"
done
