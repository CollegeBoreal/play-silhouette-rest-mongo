#!/bin/bash
function main {
local databaseName="play-silhouette-rest-mongo"
local hostAddress="localhost"
local portNumber="27017"
local scriptsFolder="./scripts"

mongo "mongodb://${hostAddress}:${portNumber}/${databaseName}" --eval "db.dropDatabase()"
mongo "mongodb://${hostAddress}:${portNumber}/${databaseName}" ${scriptsFolder}/createSchema.js
mongoimport --db ${databaseName} -h ${hostAddress}:${portNumber} --collection users --file ${scriptsFolder}/users.json --jsonArray
mongoimport --db ${databaseName} -h ${hostAddress}:${portNumber} --collection passwords --file ${scriptsFolder}/passwords.json --jsonArray
}
main
