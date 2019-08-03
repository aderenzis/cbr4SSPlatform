#!/bin/bash
docker-compose up mongo &
MONGO_PID=$!

sleep 5
echo "Parsing cases"

/usr/lib/jvm/java-8-openjdk-amd64/bin/java -jar /app/jars/SoaMLparsers.jar |& tee /app/SoaMLParsers.log

kill $MONGO_PID

sleep 1

echo "Done"
echo ""

