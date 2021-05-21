#!/bin/bash

mvn exec:java -Dexec.mainClass="org.aksw.simba.lemming.tools.BuildBaselineGraph" -Dexec.args="-ds $1 -nv $2"
