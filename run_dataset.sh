#!/bin/bash

dataset=$1
opt_iterations=100000
if [ dataset = 'pg' ]
then 
	nv=792923
elif [ dataset = 'swdf' ]
then 
	nv=45398
elif [ dataset = 'lgeo' ]
then 
	nv=591649
elif [ dataset = 'geology' ]
then 
	nv=1423
else
	echo "Invalid dataset"
	exit
fi

mkdir -p initial

count=3
for i in $(seq $count); do
  mvn exec:java -Dexec.mainClass="org.aksw.simba.lemming.tools.GraphGenerationTest" -Dexec.args="-ds $dataset -nv $nv -thrs 4 -c UCS -v UIS -op $opt_iterations"
  mvn exec:java -Dexec.mainClass="org.aksw.simba.lemming.tools.GraphGenerationTest" -Dexec.args="-ds $dataset -nv $nv -thrs 4 -c UCS -v BIS -op $opt_iterations"
  mvn exec:java -Dexec.mainClass="org.aksw.simba.lemming.tools.GraphGenerationTest" -Dexec.args="-ds $dataset -nv $nv -thrs 4 -c BCS -v UIS -op $opt_iterations"
  mvn exec:java -Dexec.mainClass="org.aksw.simba.lemming.tools.GraphGenerationTest" -Dexec.args="-ds $dataset -nv $nv -thrs 4 -c BCS -v BIS -op $opt_iterations"
  mvn exec:java -Dexec.mainClass="org.aksw.simba.lemming.tools.GraphGenerationTest" -Dexec.args="-ds $dataset -nv $nv -thrs 4 -c CCS -v UIS -op $opt_iterations"
  mvn exec:java -Dexec.mainClass="org.aksw.simba.lemming.tools.GraphGenerationTest" -Dexec.args="-ds $dataset -nv $nv -thrs 4 -c CCS -v BIS -op $opt_iterations"
done

