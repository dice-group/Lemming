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

./run_lemming.sh $dataset $nv RD intermediate/RD1_${dataset}_IntResults.ser $opt_iterations
./run_lemming.sh $dataset $nv RD intermediate/RD2_${dataset}_IntResults.ser $opt_iterations
./run_lemming.sh $dataset $nv RD intermediate/RD3_${dataset}_IntResults.ser $opt_iterations
./run_lemming.sh $dataset $nv R intermediate/R1_${dataset}_IntResults.ser $opt_iterations
./run_lemming.sh $dataset $nv R intermediate/R2_${dataset}_IntResults.ser $opt_iterations
./run_lemming.sh $dataset $nv R intermediate/R3_${dataset}_IntResults.ser $opt_iterations
./run_lemming.sh $dataset $nv C intermediate/C1_${dataset}_IntResults.ser $opt_iterations
./run_lemming.sh $dataset $nv C intermediate/C2_${dataset}_IntResults.ser $opt_iterations
./run_lemming.sh $dataset $nv C intermediate/C3_${dataset}_IntResults.ser $opt_iterations
./run_lemming.sh $dataset $nv CD intermediate/CD1_${dataset}_IntResults.ser $opt_iterations
./run_lemming.sh $dataset $nv CD intermediate/CD2_${dataset}_IntResults.ser $opt_iterations
./run_lemming.sh $dataset $nv CD intermediate/CD3_${dataset}_IntResults.ser $opt_iterations
./run_lemming.sh $dataset $nv D intermediate/D1_${dataset}_IntResults.ser $opt_iterations
./run_lemming.sh $dataset $nv D intermediate/D2_${dataset}_IntResults.ser $opt_iterations
./run_lemming.sh $dataset $nv D intermediate/D3_${dataset}_IntResults.ser $opt_iterations
./run_lemming.sh $dataset $nv DD intermediate/DD1_${dataset}_IntResults.ser $opt_iterations
./run_lemming.sh $dataset $nv DD intermediate/DD2_${dataset}_IntResults.ser $opt_iterations
./run_lemming.sh $dataset $nv DD intermediate/DD3_${dataset}_IntResults.ser $opt_iterations
./run_baseline.sh $dataset $nv
./run_baseline.sh $dataset $nv
./run_baseline.sh $dataset $nv
