// Update previously created profiles from 8.1.0
dbIam.profiles.updateMany(
    {
        name: {
            $regex: "^Profil pour la consultation des ontologies et gestion des schémas"
        }
    },
    {
        $set: {
            name: "Ontologie - gestion",
            description: "Profil pour la consultation et la gestion des ontologies et schémas dans Vitam"
        }
    }
);
