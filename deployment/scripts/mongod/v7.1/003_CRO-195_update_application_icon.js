dbIam.applications.update(
    { "identifier": "SECURITY_PROFILES_APP" },
    {
        $set: {
            "icon": "vitamui-icon vitamui-icon-security-profile"
        }
    }
);
