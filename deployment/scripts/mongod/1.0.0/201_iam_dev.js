// ----------------------------------------- ALL LEVEL -----------------------------------------
// all nominative users have 'password' as their password

// admin_user has a group with all profiles in our dev environment
dbIam.users.updateOne(
    { "_id": "admin_user" },
    {
        $set: {
            "groupId": "5c79022e7884583d1ebb6e5d0bc0121822684250a3fd2996fd93c04634363363"
        }
    }
);

dbIam.users.updateMany(
    {
        "type": "NOMINATIVE",
        "_id": {
            $ne: "casuser"
        }
    },
    {
        $set: {
            "password": "$2a$10$zdNNKv2JbrrCnE4WPje4oOW/mI.mrH0jiH1rvx0yv94HB3vLFTrni"
        }
    }
);
