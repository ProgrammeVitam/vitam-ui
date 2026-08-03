dbIam.profiles.find({ applicationName: "USERS_APP" }).forEach((profile) => {
    var distinct_roles = [...new Set(profile.roles.map((role) => role.name))].map(
        (role) => {
            return { name: role };
        }
    );

    // Update only roles whith duplication
    if (distinct_roles.length !== profile.roles.length) {
        dbIam.profiles.updateOne(
            { "_id": profile._id },
            {
                $set: {
                    "roles": distinct_roles
                }
            }
        );
    }
});
