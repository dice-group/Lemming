#!/bin/bash

dataset=$1
thrs=$2
mode='graph'
opt_iterations=100000

if [ $dataset = 'swdf' ]
then 
	nv=45221
elif [ $dataset = 'lgeo' ]
then 
	nv=591237
elif [ $dataset = 'geology' ]
then 
	nv=1243
else
	echo "Invalid dataset"
	exit
fi

mkdir -p initial
count=3
for i in $(seq $count); do
  java -jar lemming.jar $mode -ds $dataset -nv $nv -thrs $thrs -m Simplex -sp BPSI -sc BCSI -op $opt_iterations
  java -jar lemming.jar $mode -ds $dataset -nv $nv -thrs $thrs -m Simplex -sp BPSI -sc UCSI -op $opt_iterations 
  java -jar lemming.jar $mode -ds $dataset -nv $nv -thrs $thrs -m Simplex -sp UPSI -sc BCSI -op $opt_iterations
  java -jar lemming.jar $mode -ds $dataset -nv $nv -thrs $thrs -m Simplex -sp UPSI -sc UCSI -op $opt_iterations
  java -jar lemming.jar $mode -ds $dataset -nv $nv -thrs $thrs -m Bl -bl BA
  java -jar lemming.jar $mode -ds $dataset -nv $nv -thrs $thrs -m Bl -bl WS
done

