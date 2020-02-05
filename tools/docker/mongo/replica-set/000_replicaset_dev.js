rs.initiate({
    "_id" : "rs0",
    "members" : [
        {
            "_id" : 0,
            "host" : "vitamui-mongo:27018",
            "priority": 1,
            "slaveDelay" : NumberLong(0),
            "votes" : 1
        }
    ]
});
