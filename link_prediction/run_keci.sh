#!/bin/bash

main_folder=$1
for subfolder in "$main_folder"/*; do
  if [ -d "$subfolder" ]; then
    echo "Processing subfolder: $subfolder"
    for file in "$subfolder"/*; do
      if [ -f "$file" ] && [[ "$file" == *.ttl ]]; then
        echo "Processing file: $file"
        base_file=$(basename "$file")
        tmp_file="${subfolder}/tmp_${base_file}"
        echo "Processing tmp file: $tmp_file"
        JAVA_HOME=jdk-17.0.11 mvn exec:java -Dexec.mainClass="org.aksw.simba.lemming.tools.CleanGraphs" -Dexec.args="$file $tmp_file" 
        log_file="${main_folder}Experiments/${base_file}.log"
        echo "Saving logs to: $log_file"
        dicee --path_single_kg $tmp_file --model Keci --num_folds_for_cv 10 --num_core 63 --backend rdflib > $log_file 2>&1
        rm $tmp_file
      fi
    done
  fi
done

