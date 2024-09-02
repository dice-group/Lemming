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
	cp example-suite.yml example-suiteTMP.yml
        sed -i -e 's,GraphName,'"$gn"',g' example-suiteTMP.yml
        sed -i -e 's,TSTORE,Fuseki,g' example-suiteTMP.yml
        sed -i -e 's,ENDPOINT,http://localhost:3030/ds/sparql,g' example-suiteTMP.yml
        ./start-iguana.sh example-suiteTMP.yml
	pkill -f fuseki
	echo "done"
done
