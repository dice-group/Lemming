#!/bin/bash

main_folder=$1
for subfolder in "$main_folder"/*; do
  if [ -d "$subfolder" ]; then
    echo "Processing subfolder: $subfolder"
    for file in "$subfolder"/*; do
      if [ -f "$file" ]; then
        echo "Processing file: $file"
        tmp_file="tmp.ttl"
        ./preprocess_file.sh $file $tmp_file
        dicee --path_single_kg $tmp_file --model Keci --num_folds_for_cv 10 --backend rdflib
        # TODO collect summary statistics to log files
        rm $tmp_file
      fi
    done
  fi
done




