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
	cp example-suite.yml example-suiteTMP.yml
	sed -i -e 's,GraphName,'"$gn"',g' example-suiteTMP.yml
	sed -i -e 's,TSTORE,Virtuoso,g' example-suiteTMP.yml
	sed -i -e 's,ENDPOINT,http://localhost:8890/sparql,g' example-suiteTMP.yml
	./start-iguana.sh example-suiteTMP.yml
	pkill -f virtuoso
	echo "done"
done
