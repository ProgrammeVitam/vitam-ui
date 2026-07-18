var collectionExists = dbIam.getCollectionNames().indexOf('subrogations') > -1;

if (collectionExists) {
    dbIam.subrogations.dropIndex("idx_subrogation_date");

    dbIam.subrogations.createIndex(
        { date: 1 },
        { expireAfterSeconds: 0, background: true, name: "idx_subrogation_date" }
    );
}
