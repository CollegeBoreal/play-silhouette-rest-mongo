#!/bin/bash
function main {

# DB connection settings
local databaseName="play-silhouette-rest-mongo"
local hostAddress="localhost"
local portNumber="27017"
local scriptsFolder="/tmp"

# Unfortunately we need to copy the script over to the container
# before running them with mongo CLI
docker cp scripts/createSchema.js some-mongo:/tmp/createSchema.js
docker cp scripts/user.json some-mongo:/tmp/user.json
docker cp scripts/password.json some-mongo:/tmp/password.json

# Run mongo CLI from container
docker exec --interactive some-mongo mongo "mongodb://${hostAddress}:${portNumber}/${databaseName}" --eval "db.dropDatabase()"
docker exec --interactive some-mongo mongo "mongodb://${hostAddress}:${portNumber}/${databaseName}" ${scriptsFolder}/createSchema.js
docker exec --interactive some-mongo mongoimport --db ${databaseName} -h ${hostAddress}:${portNumber} --collection user --file ${scriptsFolder}/user.json --jsonArray
docker exec --interactive some-mongo mongoimport --db ${databaseName} -h ${hostAddress}:${portNumber} --collection password --file ${scriptsFolder}/password.json --jsonArray
}
main
