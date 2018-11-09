#!/bin/bash -e
echo "Starting experiment"

sleep 5

/usr/lib/jvm/java-8-openjdk-amd64/bin/java -jar /app/jars/SoaMLparsers.jar |& tee /app/SoaMLParsers.log

echo "Starting CBR"

sleep 5

/usr/lib/jvm/java-8-openjdk-amd64/bin/java -Xmx2g -jar /app/jars/cbr4SSPlatform.jar

echo "Done"

