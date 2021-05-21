#!/bin/bash

mvn exec:java -Dexec.mainClass="org.aksw.simba.lemming.tools.GraphGenerationTest" -Dexec.args="-ds $1 -nv $2 -t $3 -l intermediate/$4 -op $5"
