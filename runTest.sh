#!/bin/bash
mvn clean compile
for numClients in 5 10 15 20 25 50 75 100 200 ; do
  for numTaxis in 5 10 15 20 25 50 75 100 200 ; do
    if [ $numClients -le $numTaxis ]; then
      echo $numClients $numTaxis
      MAVEN_OPTS="-Xmx4096M" mvn exec:java -Dexec.mainClass="pl.edu.pw.elka.taxiAgents.EfficiencyTest" -Dexec.args="${numTaxis} ${numClients}"
    fi
done
done