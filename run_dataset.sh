#!/bin/bash

dataset=$1

if [ dataset = 'pg' ]
then 
	nv=792923
	opt_iterations=30000
elif [ dataset = 'swdf' ]
then 
	nv=45423
	opt_iterations=50000
elif [ dataset = 'lgeo' ]
then 
	nv=591649
	opt_iterations=50000
elif [ dataset = 'geology' ]
then 
	nv=1423
	opt_iterations=50000
else
	echo "Invalid dataset"
	exit
fi

mkdir -p intermediate
mvn clean install

./run_lemming.sh $dataset $nv RD RD1_${dataset}_IntResults.ser $opt_iterations
./run_lemming.sh $dataset $nv RD RD2_${dataset}_IntResults.ser $opt_iterations
./run_lemming.sh $dataset $nv RD RD3_${dataset}_IntResults.ser $opt_iterations
./run_lemming.sh $dataset $nv R R1_${dataset}_IntResults.ser $opt_iterations
./run_lemming.sh $dataset $nv R R2_${dataset}_IntResults.ser $opt_iterations
./run_lemming.sh $dataset $nv R R3_${dataset}_IntResults.ser $opt_iterations
./run_lemming.sh $dataset $nv C C1_${dataset}_IntResults.ser $opt_iterations
./run_lemming.sh $dataset $nv C C2_${dataset}_IntResults.ser $opt_iterations
./run_lemming.sh $dataset $nv C C3_${dataset}_IntResults.ser $opt_iterations
./run_lemming.sh $dataset $nv CD CD1_${dataset}_IntResults.ser $opt_iterations
./run_lemming.sh $dataset $nv CD CD2_${dataset}_IntResults.ser $opt_iterations
./run_lemming.sh $dataset $nv CD CD3_${dataset}_IntResults.ser $opt_iterations
./run_lemming.sh $dataset $nv D D1_${dataset}_IntResults.ser $opt_iterations
./run_lemming.sh $dataset $nv D D2_${dataset}_IntResults.ser $opt_iterations
./run_lemming.sh $dataset $nv D D3_${dataset}_IntResults.ser $opt_iterations
./run_lemming.sh $dataset $nv DD DD1_${dataset}_IntResults.ser $opt_iterations
./run_lemming.sh $dataset $nv DD DD2_${dataset}_IntResults.ser $opt_iterations
./run_lemming.sh $dataset $nv DD DD3_${dataset}_IntResults.ser $opt_iterations
./run_baseline.sh $dataset $nv
./run_baseline.sh $dataset $nv
./run_baseline.sh $dataset $nv
