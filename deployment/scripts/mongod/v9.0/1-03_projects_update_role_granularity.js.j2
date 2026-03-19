dbIam = db.getSiblingDB('{{ mongodb.iam.db | default('iam') }}')
dbSecurity = db.getSiblingDB('{{ mongodb.security.db | default('security') }}')

print("START v9.0.1-03_projects_update_role_granularity");

// First, rename "Profile Service producteur" to "Service producteur" for homogeneity
dbIam.profiles.updateOne(
    { name: "Profile Service producteur" },
    { $set : { name: "Service producteur" } }
);

// Then, delete old "ROLE_UPDATE_PROJECTS" role
dbIam.profiles.updateMany(
    {
         $or: [
            { name: { $regex: "^Archiviste - Administrateur" } },
            { name: { $regex: "^Archiviste - gestion et collecte" } },
            { name: { $regex: "^Service producteur" } }
        ]
    },
    {
        $pull: {
            roles: { name: 'ROLE_UPDATE_PROJECTS' }
        }
    }
);


// Finally, add new projects roles on Archiviste - Administrateur
dbIam.profiles.updateMany(
    { name: { $regex: "^Archiviste - Administrateur" } },
    {
        $addToSet: {
            roles: {
                $each: [
                    { name: "ROLE_UPDATE_PROJECTS_DESCRIPTION" },
                    { name: "ROLE_UPDATE_PROJECTS_CONTEXT" },
                    { name: "ROLE_UPDATE_PROJECTS_ATTACHMENT" },
                    { name: "ROLE_UPDATE_PROJECTS_CONFIG" }
                ]
            }
        }
    }
);

// Finally, add new projects roles on Archiviste - gestion et collecte
dbIam.profiles.updateMany(
    { name: { $regex: "^Archiviste - gestion et collecte" } },
    {
        $addToSet: {
            roles: {
                $each: [
                    { name: "ROLE_UPDATE_PROJECTS_DESCRIPTION" },
                    { name: "ROLE_UPDATE_PROJECTS_CONTEXT" },
                    { name: "ROLE_UPDATE_PROJECTS_ATTACHMENT" }
                ]
            }
        }
    }
);

// Finally, add new projects roles on Service producteur
dbIam.profiles.updateMany(
    { name: { $regex: "^Service producteur" } },
    {
        $addToSet: {
            roles: {
                $each: [
                    { name: "ROLE_UPDATE_PROJECTS_DESCRIPTION" }
                ]
            }
        }
    }
);

// Remove old project role "ROLE_UPDATE_PROJECTS" on collect-ui context
dbSecurity.contexts.updateOne(
    { "_id": "ui_collect_context" },
    {
        $pull: {
            roleNames: "ROLE_UPDATE_PROJECTS"
        }
    }
);

// Add new project roles on collect-ui context
dbSecurity.contexts.updateOne(
    { "_id": "ui_collect_context" },
    {
        $addToSet: {
            roleNames: {
                $each: [
                    "ROLE_UPDATE_PROJECTS_DESCRIPTION",
                    "ROLE_UPDATE_PROJECTS_CONTEXT",
                    "ROLE_UPDATE_PROJECTS_ATTACHMENT",
                    "ROLE_UPDATE_PROJECTS_CONFIG"
                ]
            }
        }
    }
);

print("END v9.0.1-03_projects_update_role_granularity");
