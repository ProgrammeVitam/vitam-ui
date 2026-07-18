print("START 321_delete_cas_token.js");

dbIam = db.getSiblingDB('{{ mongodb.iam.db | default('iam') }}')

dbIam.tokens.deleteMany({ "refId": "casuser" });

print("END 321_delete_cas_token.js");
