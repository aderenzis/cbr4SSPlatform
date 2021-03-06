#!/bin/bash
docker-compose up mongo &
MONGO_PID=$!
sleep 10

docker-compose run --rm -v "$PWD"/RESULTS:/backup mongo sh -c 'mongoexport --out /backup/results.json --host mongo:27017 --pretty --jsonArray --db results --collection retrievedCase'

kill $MONGO_PID
sleep 1

echo "Done, results saved in $PWD/RESULTS"
echo ""
