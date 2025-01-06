db = db.getSiblingDB('iam');
dbSecurity = db.getSiblingDB('security');

print('START 63_collect_missing_role.js');

// ---- ROLE_GET_UNITS will be added to profiles including COLLECT_APP --- //

db.profiles.updateMany(
    { 'applicationName': 'COLLECT_APP' },
    { '$addToSet': { 'roles': { $each: [{ 'name': 'ROLE_GET_UNITS' }] } } }
);

dbSecurity.contexts.updateOne(
    { '_id': 'ui_collect_context' },
    { $addToSet: { 'roleNames': 'ROLE_GET_UNITS' } }
);

print('END 63_collect_missing_role.js');
