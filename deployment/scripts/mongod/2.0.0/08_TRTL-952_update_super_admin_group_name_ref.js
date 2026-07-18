dbIam.groups.updateOne(
    { "_id": "super_admin_group" },
    {
        $set: {
            "name": "Groupe de l'administrateur de l'instance",
            "description": "Groupe de l'administrateur de l'instance"
        },
    }
);
