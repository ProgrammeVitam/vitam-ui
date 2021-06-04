print('START 009_TRTLE-878_setting_up_userinfo_ref.js');
db = db.getSiblingDB('iam');

db.profiles.updateMany(
    { "applicationName": "USERS_APP" },
    { $addToSet: { "roles": { $each: [
                    { "name": "ROLE_GET_USER_INFOS" },
                    { "name": "ROLE_CREATE_USER_INFOS" },
                    { "name": "ROLE_UPDATE_USER_INFOS" }
                ]}}}
);

print('END 009_TRTLE-878_setting_up_userinfo_ref.js');
