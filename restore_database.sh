#!/bin/bash
docker-compose up mongo &
MONGO_PID=$!

sleep 10

docker-compose run --rm -v "$PWD"/KB:/backup mongo sh -c 'mongorestore /backup --host mongo:27017'

kill $MONGO_PID

sleep 1

echo "Done"
echo ""

