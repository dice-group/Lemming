graphFolder=$1
for f in $(ls $graphFolder)
do
	fullFile=$graphFolder$f
	rm -rf queryInstances/
	echo "executing $f"
	pkill -f blazegraph
	./start-ref.sh $fullFile
	./start-blazegraph.sh $fullFile
	sleep 5m
	#gn=$(echo ${f:43} | sed -e 's/[^a-zA-Z0-9]//g')
	#gn="geology_target"
	gn="${f%%.*}"
	echo "$gn"
	cp example-suite.yml example-suiteTMP.yml
        sed -i -e 's,GraphName,'"$gn"',g' example-suiteTMP.yml
        sed -i -e 's,TSTORE,Blazegraph,g' example-suiteTMP.yml
        sed -i -e 's,ENDPOINT,http://localhost:9999/blazegraph/sparql,g' example-suiteTMP.yml
        ./start-iguana.sh example-suiteTMP.yml

	pkill -f blazegraph
	echo "done"
done
