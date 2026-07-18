db = db.getSiblingDB('{{ mongodb.archivesearch.db }}')

print("START 06_create_search_criteria_history.js");

db.searchCriteriaHistories.deleteMany({});
db.createCollection('searchCriteriaHistories', {autoIndexId: true});

print("END 06_create_search_criteria_history.js");
