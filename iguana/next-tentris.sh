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
	cp example-suite.yml example-suiteTMP.yml
	sed -i -e 's,GraphName,'"$gn"',g' example-suiteTMP.yml
	sed -i -e 's,TSTORE,Tentris,g' example-suiteTMP.yml
	sed -i -e 's,ENDPOINT,http://localhost:9080/sparql,g' example-suiteTMP.yml
	./start-iguana.sh example-suiteTMP.yml
	pkill -f tentris
	echo "done"
done
