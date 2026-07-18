dbIam.applications.updateOne(
    { identifier: 'EXTERNAL_PARAM_PROFILE_APP' },
    {
        $set: {
            icon: "vitamui-icon vitamui-icon-external-param-profil"
        }
    }
);
