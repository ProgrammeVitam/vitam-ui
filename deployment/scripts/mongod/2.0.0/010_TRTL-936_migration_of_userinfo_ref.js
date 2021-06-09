db = db.getSiblingDB('iam')

print("START 010_TRTL-936_migration_of_userinfo_ref.js");


db.users.find({"userInfoId" : {$exists : false}}).forEach(user => {

    var language = "FRENCH";

    if (user.language) {
        language = user.language
    }

    var userInfoId = new ObjectId().valueOf() + new ObjectId().valueOf();

    db.userInfos.insert({
        "_id": userInfoId,
        "language": language,
        "_class": "userInfos"
    });

    db.users.update(
        {_id: user._id},
        {
            "$set": {"userInfoId": userInfoId},
        }
    );

    db.users.update(
        {_id: user._id},
        {
            $unset: {"language": ""},
        }
    );
});

// Add user infos role to ui_admin_identity_context and ui_identity_context
db = db.getSiblingDB('security')
db.contexts.updateMany({
        '_id': {
            $in: [
                'ui_admin_identity_context',
                'ui_identity_context'
            ]
        }
    },
    {
        "$push": {
            "roleNames": {
                "$each":
                    [
                        "ROLE_GET_USER_INFOS",
                        "ROLE_CREATE_USER_INFOS",
                        "ROLE_UPDATE_USER_INFOS"

                    ]
            }
        }
    }
);
print("END 010_TRTL-936_migration_of_userinfo_ref.js");

