// https://www.mongodb.com/docs/v5.0/tutorial/manage-mongodb-processes/#use-shutdownserver--

db = db.getSiblingDB('admin');
db.shutdownServer();
