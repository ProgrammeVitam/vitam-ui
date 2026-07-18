// Create collection only if it does not already exists
if (!dbIam.getCollectionNames().includes("externalParameters")) {
    dbIam.createCollection('externalParameters', { autoIndexId: true });
}
