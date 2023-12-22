print('START 30_CRO-234_create_connection_history_with_ttl_index_ref.js');

db = db.getSiblingDB('iam');

var collectionExists = db.getCollectionNames().indexOf('connectionHistory') > -1;

if (!collectionExists) {
	db.createCollection("connectionHistory");
	db.connectionHistory.createIndex({ "connectionDateTime": 1 }, { expireAfterSeconds: 63115200 })
}

print('END 30_CRO-234_create_connection_history_with_ttl_index_ref.js');
