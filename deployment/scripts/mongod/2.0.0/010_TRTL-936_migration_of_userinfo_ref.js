dbIam.users.find({ "userInfoId": { $exists: false } }).forEach(user => {

    var language = "FRENCH";

    if (user.language) {
        language = user.language
    }

    var userInfoId = new ObjectId().valueOf() + new ObjectId().valueOf();

    dbIam.userInfos.insertOne(
        {
            "_id": userInfoId,
            "language": language,
            "_class": "userInfos"
        }
    );

    dbIam.users.updateOne(
        { "_id": user._id },
        {
            $set: {
                "userInfoId": userInfoId
            },
        }
    );

    dbIam.users.updateOne(
        { "_id": user._id },
        {
            $unset: {
                "language": ""
            },
        }
    );
});

// Add user infos role to ui_admin_identity_context and ui_identity_context
dbSecurity.contexts.updateMany(
    {
        "_id": {
            $in: [
                "ui_admin_identity_context",
                "ui_identity_context"
            ]
        }
    },
    {
        $addToSet: {
            "roleNames": {
                $each: [
                    "ROLE_GET_USER_INFOS",
                    "ROLE_CREATE_USER_INFOS",
                    "ROLE_UPDATE_USER_INFOS",
                    "ROLE_GET_EXTERNAL_PARAMS"
                ]
            }
        }
    }
);
