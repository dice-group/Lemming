#!/bin/bash

# Example usage:
# bash generate_graphs.sh swdf 32

dataset="$1"			# dataset name
thrs=$2 				# number of threads
mode='graph' 			# graph for versioned input, single-graph for single graph input, or store to compute graph
opt_iterations=50000	# number of optimization steps

# set target number of vertices
if [ "$dataset" = 'pg' ]
then 
	nv=792923
elif [ "$dataset" = 'swdf' ]
then 
	nv=45398
elif [ "$dataset" = 'lgeo' ]
then 
	nv=591649
elif [ "$dataset" = 'geology' ]
then 
	nv=1423
elif [ "$dataset" = 'dbp' ]
then 
	nv=7942015
else
	echo "Invalid dataset"
	exit
fi

# run each 3 times
mkdir -p initial
count=3
for _ in $(seq $count); do
  java -jar lemming.jar "$mode" -ds "$dataset" -nv "$nv" -thrs "$thrs" -c UCS -v UIS -op "$opt_iterations"
  java -jar lemming.jar "$mode" -ds "$dataset" -nv "$nv" -thrs "$thrs" -c UCS -v BIS -op "$opt_iterations"
  java -jar lemming.jar "$mode" -ds "$dataset" -nv "$nv" -thrs "$thrs" -c BCS -v UIS -op "$opt_iterations"
  java -jar lemming.jar "$mode" -ds "$dataset" -nv "$nv" -thrs "$thrs" -c BCS -v BIS -op "$opt_iterations"
  java -jar lemming.jar "$mode" -ds "$dataset" -nv "$nv" -thrs "$thrs" -c CCS -v UIS -op "$opt_iterations"
  java -jar lemming.jar "$mode" -ds "$dataset" -nv "$nv" -thrs "$thrs" -c CCS -v BIS -op "$opt_iterations"
  java -jar lemming.jar "$mode" -ds "$dataset" -nv "$nv" -thrs "$thrs" -m Simplex -sp BPSI -sc BCSI -op "$opt_iterations"
  java -jar lemming.jar "$mode" -ds "$dataset" -nv "$nv" -thrs "$thrs" -m Simplex -sp BPSI -sc UCSI -op "$opt_iterations" 
  java -jar lemming.jar "$mode" -ds "$dataset" -nv "$nv" -thrs "$thrs" -m Simplex -sp UPSI -sc BCSI -op "$opt_iterations"
  java -jar lemming.jar "$mode" -ds "$dataset" -nv "$nv" -thrs "$thrs" -m Simplex -sp UPSI -sc UCSI -op "$opt_iterations"
  java -jar lemming.jar "$mode" -ds "$dataset" -nv "$nv" -thrs "$thrs" -m Bl -bl BA
  java -jar lemming.jar "$mode" -ds "$dataset" -nv "$nv" -thrs "$thrs" -m Bl -bl WS

done

