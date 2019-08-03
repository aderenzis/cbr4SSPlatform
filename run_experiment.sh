#!/bin/bash -e
echo "Starting experiment"

./load_cases.sh

echo "Starting CBR"

sleep 5

/usr/lib/jvm/java-8-openjdk-amd64/bin/java -Xmx2g -jar /app/jars/cbr4SSPlatform.jar

echo "Done"

