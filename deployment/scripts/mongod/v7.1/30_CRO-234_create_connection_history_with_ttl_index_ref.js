var collectionExists = dbIam.getCollectionNames().indexOf('connectionHistory') > -1;

if (!collectionExists) {
    dbIam.createCollection("connectionHistory");
    dbIam.connectionHistory.createIndex({ "connectionDateTime": 1 }, { expireAfterSeconds: 63115200 })
}
