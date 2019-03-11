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
docker cp scripts/users.json some-mongo:/tmp/users.json
docker cp scripts/passwords.json some-mongo:/tmp/passwords.json

# Run mongo CLI from container
docker exec --interactive some-mongo mongo "mongodb://${hostAddress}:${portNumber}/${databaseName}" --eval "db.dropDatabase()"
docker exec --interactive some-mongo mongo "mongodb://${hostAddress}:${portNumber}/${databaseName}" ${scriptsFolder}/createSchema.js
docker exec --interactive some-mongo mongoimport --db ${databaseName} -h ${hostAddress}:${portNumber} --collection users --file ${scriptsFolder}/users.json --jsonArray
docker exec --interactive some-mongo mongoimport --db ${databaseName} -h ${hostAddress}:${portNumber} --collection passwords --file ${scriptsFolder}/passwords.json --jsonArray
}
main
