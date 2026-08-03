dbIam.profiles.updateMany(
    { "roles.name": "ROL_GET_PASTIS" },
    { $set: { "roles.$[elem].name": "ROLE_GET_PASTIS" } },
    {
        arrayFilters: [
            { "elem.name": "ROL_GET_PASTIS" }
        ]
    }
);
